package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.SimpleAmqpQueue;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.utils.SqsQueueUtility;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    private SqsQueueUtility sqsQueueUtility;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;

    private static final String NHS_NUMBER = "9798548754";
    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("ce3aad10-9b7c-4a9b-ab87-a9d6521d61b2");
    private static final UUID SMALL_EHR_INBOUND_CONVERSATION_ID = UUID.fromString("ff27abc3-9730-40f7-ba82-382152e6b90a");
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "B14758";

    @BeforeEach
    void beforeEach(@Value("${inboundTimeoutSeconds}") String inboundTimeoutSeconds) {
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
        stubFor(post(
                urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204))
        );

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
        stubFor(post(
                urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204))
        );

        sqsQueueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);
        waitForConversationTransferStatusMatching(INBOUND_REQUEST_SENT);


        // then
        waitForConversationTransferStatusMatching(INBOUND_TIMEOUT);
    }

    @Test
    void Given_ValidRepoIncomingEventForLargeEhr_When_CoreReceivedButNoFragments_Then_UpdateStatusToInboundTimeout() throws IOException {
        // given
        // override inboundTimeoutSeconds so that the request will timeout within the timeframe
        System.setProperty("inboundTimeoutSeconds", "10");

        final SimpleAmqpQueue inboundQueueFromMhs = new SimpleAmqpQueue(inboundQueue);
        final String repoIncomingMessage = getRepoIncomingMessage();
        final String largeEhrCore = getTestDataAsString("large-ehr-core");

        // when
        stubFor(post(
                urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204))
        );

        sqsQueueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);
        waitForConversationTransferStatusMatching(INBOUND_REQUEST_SENT);

        inboundQueueFromMhs.sendMessage(largeEhrCore);
        waitForConversationTransferStatusMatching(INBOUND_CONTINUE_REQUEST_SENT);


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

    private void waitForConversationTransferStatusMatching(ConversationTransferStatus transferStatus) {
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            final ConversationRecord record = transferService
                    .getConversationByInboundConversationId(INBOUND_CONVERSATION_ID);

            assertEquals(transferStatus.name(), record.state());
        });
    }
}