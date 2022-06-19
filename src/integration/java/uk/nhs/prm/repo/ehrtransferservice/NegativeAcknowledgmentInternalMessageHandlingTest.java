package uk.nhs.prm.repo.ehrtransferservice;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
@Disabled("WIP")
public class NegativeAcknowledgmentInternalMessageHandlingTest {

    @Autowired
    TransferTrackerDb transferTrackerDb;

    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${aws.nackQueueName}")
    private String nackInternalQueueName;

    @Value("aws.transferCompleteQueueName")
    private String transferCompleteQueue;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @AfterEach
    public void tearDown() {
        purgeQueue(nackInternalQueueName);
        purgeQueue(transferCompleteQueue);
    }

    @Test
    public void shouldUpdateDbWithNackErrorCodeWhenReceivedOnInternalQueue() throws IOException, InterruptedException {
        var attachment = dataLoader.getDataAsString("MCCI_IN010000UK13FailureSanitized");
        UUID transferConversationId = createTransferRecord();

        sendMessage(attachment, nackInternalQueueName);

        var transferState = fetchTransferState(transferConversationId);

        assertThat(transferState.getState()).isEqualTo("ACTION:EHR_TRANSFER_FAILED:15");

    }

    private UUID createTransferRecord() {
        var conversationId = UUID.fromString("13962cb7-6d46-4986-bdb4-3201bb25f1f7");
        TransferTrackerDbEntry transferTrackerDbEntry =
                new TransferTrackerDbEntry(conversationId.toString(),
                        "0123456789",
                        "BOB13",
                        UUID.randomUUID().toString(),
                        trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings(),
                        "great status",
                        trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings(), UUID.randomUUID().toString());
        transferTrackerDb.save(transferTrackerDbEntry);
        return conversationId;
    }

    private String trustMeToGetTimeNowInTheRightFormatCauseWeLikeStrings() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }

    private void sendMessage(String message, String queueName) {
        GetQueueUrlResult queueUrl = sqs.getQueueUrl(queueName);
        sqs.sendMessage(queueUrl.getQueueUrl(), message);
    }

    private TransferTrackerDbEntry fetchTransferState(UUID conversationId) {
        return transferTrackerDb.getByConversationId(conversationId.toString());
    }

    private void purgeQueue(String queueName) {
        var queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}
