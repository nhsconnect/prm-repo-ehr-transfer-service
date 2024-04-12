package uk.nhs.prm.repo.ehrtransferservice.handler;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
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
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ExtendWith(ForceXercesParserExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class NegativeAcknowledgmentHandlingIntegrationTest {
    @Autowired
    TransferService transferService;

    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${aws.nackQueueName}")
    private String nackInternalQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String DESTINATION_GP = "A74854";
    private static final String CONVERSATION_ID = "13962CB7-6D46-4986-BDB4-3201BB25F1F7";
    private static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";

    @BeforeEach
    public void tearDown() {
        purgeQueue(nackInternalQueueName);
    }

    @Test
    void shouldUpdateDbWithNackErrorCodeWhenReceivedOnInternalQueue() throws IOException {
        // given
        final String negativeAck = dataLoader.getDataAsString("MCCI_IN010000UK13Failure");
        final UUID inboundConversationId = createConversationRecord();
        final SimpleAmqpQueue inboundQueueFromMhs = new SimpleAmqpQueue(inboundQueue);

        // when
        inboundQueueFromMhs.sendMessage(negativeAck);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            String transferStatus = transferService
                .getConversationTransferStatus(inboundConversationId);

            assertThat(transferStatus).isEqualTo(INBOUND_FAILED.name());
        });
    }

    private UUID createConversationRecord() {
        RepoIncomingEvent event = new RepoIncomingEvent(
            NHS_NUMBER,
            SOURCE_GP,
            NEMS_MESSAGE_ID,
            DESTINATION_GP,
            NEMS_EVENT_LAST_UPDATED,
            CONVERSATION_ID
        );

        transferService.createConversation(event);
        return UUID.fromString(event.getConversationId());
    }

    private void purgeQueue(String queueName) {
        final String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        final PurgeQueueRequest purgeQueueRequest = new PurgeQueueRequest(queueUrl);

        sqs.purgeQueue(purgeQueueRequest);
    }
}