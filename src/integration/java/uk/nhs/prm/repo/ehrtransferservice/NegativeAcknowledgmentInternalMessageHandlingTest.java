package uk.nhs.prm.repo.ehrtransferservice;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

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

    @Autowired
    private DynamoDbClient dbClient;

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.nackQueueName}")
    private String nackInternalQueueName;

    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    @Test
    public void shouldUpdateDbWithNackErrorCodeWhenReceivedOnInternalQueue() {

        UUID transferConversationId = createTransferRecord();

        String internalNackMessage = new Gp2gpNackBuilder()
                .withConversationId(transferConversationId)
                .withErrorCode("06")
                .withErrorDisplayText("Well that went wrong.")
                .build();

        sendMessage(internalNackMessage, nackInternalQueueName);

        var transferState = fetchTransferState(transferConversationId);

        assertThat(transferState.getState()).isEqualTo("ACTION:EHR_TRANSFER_FAILED:06");
    }

    private UUID createTransferRecord() {
        var conversationId = UUID.randomUUID();
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
}
