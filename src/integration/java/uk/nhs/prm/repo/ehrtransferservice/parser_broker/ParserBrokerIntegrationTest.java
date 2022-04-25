package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
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
@Ignore
public class ParserBrokerIntegrationTest {
    @Autowired
    private AmazonSQSAsync sqs;

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${aws.attachmentsQueueName}")
    private String attachmentsQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @Test
    void shouldPublishAttachmentToAttachmentTopic() throws IOException, InterruptedException {
        String attachment = dataLoader.getDataAsString("COPC_IN000001UK01");
        String attachmentSanitized = dataLoader.getDataAsString("COPC_IN000001UK01Sanitized");

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(attachment.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
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

    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
        System.out.println("checking sqs queue: " + queueUrl);

        var requestForMessagesWithAttributes
                = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMessageAttributeNames("traceId");
        var messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
        assertThat(messages).hasSize(1);
        return messages;
    }
}
