package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.utils.QueueUtility;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.Layer.CONVERSATION;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.DESTINATION_GP;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8080)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
class EhrRequestTest {
    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    private QueueUtility queueUtility;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;

    private static final String NHS_NUMBER = "9798548754";
    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("ce3aad10-9b7c-4a9b-ab87-a9d6521d61b2");
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "B14758";

    @AfterEach
    void afterEach() {
        queueUtility.purgeQueue(repoIncomingQueueName);
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

        queueUtility.sendSqsMessage(repoIncomingMessage, repoIncomingQueueName);

        // then
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            final ConversationRecord record = transferService
                .getConversationByInboundConversationId(INBOUND_CONVERSATION_ID);

            assertEquals(INBOUND_REQUEST_SENT.name(), record.state());
        });
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
            INBOUND_CONVERSATION_ID,
            NHS_NUMBER,
            NEMS_MESSAGE_ID,
            SOURCE_GP,
            DESTINATION_GP,
            NEMS_EVENT_LAST_UPDATED
        );
    }
}