package uk.nhs.prm.repo.ehrtransferservice;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class NegativeAcknowledgmentHandlingIntegrationTest {

    @Autowired
    TransferTrackerDb transferTrackerDb;

    @Autowired
    private AmazonSQSAsync sqs;

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${aws.nackQueueName}")
    private String nackInternalQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @BeforeEach
    public void tearDown() {
        purgeQueue(nackInternalQueueName);
    }

    @Disabled("We need to create the byteMessage properly, possibly using the proton library")
    @Test
    public void shouldUpdateDbWithNackErrorCodeWhenReceivedOnInternalQueue() throws IOException {
        var negativeAck = dataLoader.getDataAsString("MCCI_IN010000UK13FailureSanitized");
        UUID transferConversationId = createTransferRecord();

        sendToQueue(negativeAck, inboundQueue);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var transferState = fetchTransferState(transferConversationId);
            assertThat(transferState.getState()).isEqualTo("ACTION:EHR_TRANSFER_FAILED:15");
        });

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

    private void sendToQueue(String negativeAck, String queueName) {
        jmsTemplate.send(queueName, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(negativeAck.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });
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
