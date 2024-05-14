package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.SimpleAmqpQueue;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.utils.SqsQueueUtility;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_CONTINUE_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_TIMEOUT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.Layer.CONVERSATION;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.DESTINATION_GP;
import static uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoaderUtility.getTestDataAsString;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8080)
@ExtendWith(SpringExtension.class)
@ExtendWith(ForceXercesParserExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class EhrRequestTest {
    private static final Logger log = LoggerFactory.getLogger(EhrRequestTest.class);
    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    private SqsQueueUtility sqsQueueUtility;

    @MockBean
    private EhrRepoService ehrRepoService;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;

    private static final String NHS_NUMBER = "9798548754";
    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("ce3aad10-9b7c-4a9b-ab87-a9d6521d61b2");
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "B14758";

    @BeforeEach
    void beforeEach(@Value("${inboundTimeoutSeconds}") String inboundTimeoutSeconds) throws Exception {
        // reset the timeout to the default as it is redefined in some tests
        System.setProperty("inboundTimeoutSeconds", inboundTimeoutSeconds);
    }

    @AfterEach
    void afterEach() {
        sqsQueueUtility.purgeQueue(repoIncomingQueueName);
        transferTrackerDbUtility.deleteItem(INBOUND_CONVERSATION_ID, CONVERSATION);
    }

    @Test
    void Given_ValidRepoIncomingEvent_When_PublishedToRepoIncomingQueue_Then_CreateConversationAndUpdateStatusToInboundRequestSent() {
        // given
        final String repoIncomingMessage = getRepoIncomingMessage();

        // when
        createStubForGp2GpMessengerEhrRequest();
        sqsQueueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);

        // then
        waitForConversationTransferStatusMatching(INBOUND_REQUEST_SENT);
    }

    @Test
    void Given_ValidRepoIncomingEventForSmallEhr_When_NoEhrResponseReceived_Then_UpdateStatusToInboundTimeout() {
        // given
        // override inboundTimeoutSeconds so that the request will timeout within the timeframe
        System.setProperty("inboundTimeoutSeconds", "10");

        final String repoIncomingMessage = getRepoIncomingMessage();

        // when
        createStubForGp2GpMessengerEhrRequest();
        sqsQueueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);
        waitForConversationTransferStatusMatching(INBOUND_REQUEST_SENT);


        // then
        waitForConversationTransferStatusMatching(INBOUND_TIMEOUT);
    }

    @Test
    void Given_ValidRepoIncomingEventForLargeEhr_When_CoreReceivedButNoFragments_Then_UpdateStatusToInboundTimeout() throws Exception {
        // given
        // override inboundTimeoutSeconds so that the request will timeout within the timeframe
        System.setProperty("inboundTimeoutSeconds", "10");

        final SimpleAmqpQueue inboundQueueFromMhs = new SimpleAmqpQueue(inboundQueue);
        final String repoIncomingMessage = getRepoIncomingMessage();
        final String largeEhrCore = getTestDataAsString("large-ehr-core");

        // when
        createStubForGp2GpMessengerEhrRequest();
        createStubForGp2GpMessengerContinueRequest();
        createMockForEhrRepoStoreMessage();

        sqsQueueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);
        waitForConversationTransferStatusMatching(INBOUND_REQUEST_SENT);

        inboundQueueFromMhs.sendMessage(largeEhrCore);
        waitForConversationTransferStatusMatching(INBOUND_CONTINUE_REQUEST_SENT);

        // TODO PRMT-4817 it's successfully sending the continue request but not timing out?
        //  org.opentest4j.AssertionFailedError: expected: <INBOUND_TIMEOUT> but was: <INBOUND_CONTINUE_REQUEST_SENT>

        // then
        waitForConversationTransferStatusMatching(INBOUND_TIMEOUT);
    }

    // Test specific helper methods
    private String getRepoIncomingMessage() {
        final String repoIncomingEvent = """
            {
                "conversationId": "%s",
                "nhsNumber": "%s",
                "nemsMessageId": "%s",
                "sourceGp": "%s",
                "destinationGp": "%s",
                "nemsEventLastUpdated": "%s"
            }
            """;

        return repoIncomingEvent.formatted(
                INBOUND_CONVERSATION_ID.toString().toUpperCase(),
                NHS_NUMBER,
                NEMS_MESSAGE_ID,
                SOURCE_GP,
                DESTINATION_GP,
                NEMS_EVENT_LAST_UPDATED
        );
    }

    private void createMockForEhrRepoStoreMessage() throws Exception {
        StoreMessageResult result = new StoreMessageResult(new StoreMessageResponseBody("complete"));
        when(ehrRepoService.storeMessage(Mockito.any())).thenReturn(result);
    }
    
    private void createStubForGp2GpMessengerEhrRequest() {
        stubFor(post(urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));
    }

    private void createStubForGp2GpMessengerContinueRequest() {
        stubFor(post(urlMatching("/health-record-requests/continue-message"))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));
    }

    private void waitForConversationTransferStatusMatching(ConversationTransferStatus transferStatus) {
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            try {
                ConversationRecord record = transferService.getConversationByInboundConversationId(INBOUND_CONVERSATION_ID);
                log.info("The current status of the Conversation is: {}", record.state());
                assertEquals(transferStatus.name(), record.state());
            } catch (ConversationNotPresentException ignored) {}
        });
    }
}