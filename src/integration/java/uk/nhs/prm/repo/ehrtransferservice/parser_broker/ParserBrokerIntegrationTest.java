package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class ParserBrokerIntegrationTest {
    @Autowired
    private AmazonSQSAsync sqs;

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${aws.attachmentsQueueName}")
    private String attachmentsQueueName;

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

    @Value("${aws.largeEhrQueueName}")
    private String largeEhrQueueName;

    @Value("${aws.parsingDlqQueueName}")
    private String parsingDlqQueueName;

    @Value("${aws.ehrCompleteQueueName}")
    private String ehrCompleteQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @AfterEach
    public void tearDown() {
        purgeQueue(attachmentsQueueName);
        purgeQueue(smallEhrQueueName);
        purgeQueue(largeEhrQueueName);
        purgeQueue(parsingDlqQueueName);
        purgeQueue(ehrCompleteQueueName);
    }

    @Test
    void shouldPublishAttachmentToAttachmentTopic() throws IOException, InterruptedException {
        var attachment = dataLoader.getDataAsString("COPC_IN000001UK01");
        var attachmentSanitized = dataLoader.getDataAsString("COPC_IN000001UK01Sanitized");

        jmsTemplate.send(inboundQueue, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(attachment.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });
        sleep(5000);

        var attachmentsQueueUrl = sqs.getQueueUrl(attachmentsQueueName).getQueueUrl();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(attachmentsQueueUrl);
            assertTrue(receivedMessageHolder.get(0).getBody().contains(attachmentSanitized));
            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
        });
    }

//    @Test
//    void shouldPublishSmallMessageToSmallTopic() throws IOException, InterruptedException {
//        var smallEhr = dataLoader.getDataAsString("RCMR_IN030000UK06");
//        var smallEhrSanitized = dataLoader.getDataAsString("RCMR_IN030000UK06Sanitized");
//
//        jmsTemplate.send(inboundQueue, session -> {
//            var bytesMessage = session.createBytesMessage();
//            bytesMessage.writeBytes(smallEhr.getBytes(StandardCharsets.UTF_8));
//            return bytesMessage;
//        });
//        sleep(5000);
//
//        var smallEhrQueueUrl = sqs.getQueueUrl(smallEhrQueueName).getQueueUrl();
//
//        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
//            var receivedMessageHolder = checkMessageInRelatedQueue(smallEhrQueueUrl);
//            assertTrue(receivedMessageHolder.get(0).getBody().contains(smallEhrSanitized));
//            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
//            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
//        });
//    }

    @Test
    void shouldPublishInvalidMessageToDlq() throws InterruptedException {
        var wrongMessage = "something wrong";

        jmsTemplate.send(inboundQueue, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(wrongMessage.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });
        sleep(5000);

        var parsingDqlQueueUrl = sqs.getQueueUrl(parsingDlqQueueName).getQueueUrl();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(parsingDqlQueueUrl);
            assertTrue(receivedMessageHolder.get(0).getBody().contains(wrongMessage));
        });
    }

    @Test
    void shouldPublishSmallEhrMessageToEhrCompleteTopic() throws IOException, InterruptedException {
        var smallEhr = dataLoader.getDataAsString("RCMR_IN030000UK06");
        var smallEhrSanitized = dataLoader.getDataAsString("RCMR_IN030000UK06Sanitized");

        jmsTemplate.send(inboundQueue, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(smallEhr.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });
        sleep(5000);

        var ehrCompleteQueueUrl = sqs.getQueueUrl(ehrCompleteQueueName).getQueueUrl();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(ehrCompleteQueueUrl);
            assertTrue(receivedMessageHolder.get(0).getBody().contains(smallEhrSanitized));
            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
            assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
        });
    }


    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
        System.out.println("checking sqs queue: " + queueUrl);

        var requestForMessagesWithAttributes
                = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMessageAttributeNames("All");
        var messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
        assertThat(messages).hasSize(1);
        return messages;
    }

    private void purgeQueue(String queueName) {
        var queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}