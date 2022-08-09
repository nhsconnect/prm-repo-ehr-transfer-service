package uk.nhs.prm.repo.ehrtransferservice;

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
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.activemq.SimpleAmqpQueue;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class NegativeAcknowledgmentHandlingIntegrationTest {

    @Autowired
    TransferTrackerDb transferTrackerDb;

    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${aws.nackQueueName}")
    private String nackInternalQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @BeforeEach
    public void tearDown() {
        purgeQueue(nackInternalQueueName);
    }

    @Test
    public void shouldUpdateDbWithNackErrorCodeWhenReceivedOnInternalQueue() throws IOException {
        var negativeAck = dataLoader.getDataAsString("MCCI_IN010000UK13FailureMessageBody");
        UUID transferConversationId = createTransferRecord();

        var inboundQueueFromMhs = new SimpleAmqpQueue(inboundQueue);
        inboundQueueFromMhs.sendMessage(negativeAck);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var transferState = fetchTransferState(transferConversationId);
            assertThat(transferState.getState()).isEqualTo("ACTION:EHR_TRANSFER_FAILED:15");
        });
    }

    private UUID createTransferRecord() {
        var conversationId = UUID.fromString("13962cb7-6d46-4986-bdb4-3201bb25f1f7");
        TransferTrackerDbEntry transferTrackerDbEntry =
                new TransferTrackerDbEntry(
                        conversationId.toString(),
                        "0123456789",
                        "BOB13",
                        UUID.randomUUID().toString(),
                        trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings(),
                        "great status",
                        trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings(),
                        UUID.randomUUID().toString(),
                        true
                );
        transferTrackerDb.save(transferTrackerDbEntry);
        return conversationId;
    }

    private String trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }

    private TransferTrackerDbEntry fetchTransferState(UUID conversationId) {
        return transferTrackerDb.getByConversationId(conversationId.toString());
    }

    private void purgeQueue(String queueName) {
        var queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}
