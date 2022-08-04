package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.CompositeWritableBuffer;
import org.apache.qpid.proton.codec.DroppingWritableBuffer;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.message.Message.Factory;
import org.apache.qpid.proton.message.ProtonJMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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

    @Value("${activemq.amqEndpoint1}")
    private String mqEndpoint1;

    @Value("${activemq.amqEndpoint2}")
    private String mqEndpoint2;

    @Value("${aws.largeMessageFragmentsObservabilityQueueName}")
    private String largeMessageFragmentsObservabilityQueueName;

    @Value("${aws.smallEhrObservabilityQueueName}")
    private String smallEhrObservabilityQueueName;

    @Value("${aws.largeEhrQueueName}")
    private String largeEhrQueueName;

    @Value("${aws.parsingDlqQueueName}")
    private String parsingDlqQueueName;

    @Value("${aws.ehrCompleteQueueName}")
    private String ehrCompleteQueueName;

    private final TestDataLoader dataLoader = new TestDataLoader();

    @AfterEach
    public void tearDown() {
        purgeQueue(largeMessageFragmentsObservabilityQueueName);
        purgeQueue(smallEhrObservabilityQueueName);
        purgeQueue(largeEhrQueueName);
        purgeQueue(parsingDlqQueueName);
        purgeQueue(ehrCompleteQueueName);
    }

    @Disabled("We need to create the byteMessage properly, possibly using the proton library")
    @Test
    void shouldPublishCopcMessageToLargeMessageFragmentTopic() throws IOException {
        var attachment = dataLoader.getDataAsString("COPC_IN000001UK01");
        var attachmentSanitized = dataLoader.getDataAsString("COPC_IN000001UK01Sanitized");

        jmsTemplate.send(inboundQueue, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(attachment.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });

        var attachmentsQueueUrl = sqs.getQueueUrl(largeMessageFragmentsObservabilityQueueName).getQueueUrl();
        System.out.println("attachmentsQueueUrl: " + attachmentsQueueUrl);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(attachmentsQueueUrl);
            Assertions.assertTrue(receivedMessageHolder.get(0).getBody().contains(attachmentSanitized));
            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
        });
    }




    private Channel getChannel() throws IOException, TimeoutException {
        var cf = new ConnectionFactory();
        var address1 = new Address(mqEndpoint1);
        var address2 = new Address(mqEndpoint2);
        var conn = cf.newConnection(List.of(address1, address2), "sdeng");

//        cf.setUsername("");
//        cf.setPassword("");
//        cf.setVirtualHost("");
//        cf.setHost("");
//        cf.setPort(61616);
//        var conn = cf.newConnection();

        return conn.createChannel();
    }

    @Disabled("WIP - building and sending message using rabbit mq client")
    @Test
    void shouldPublishSmallMessageToSmallEhrObservabilityQueue() throws IOException, TimeoutException {
        var smallEhr = dataLoader.getDataAsString("RCMR_IN030000UK06");
        var smallEhrSanitized = dataLoader.getDataAsString("RCMR_IN030000UK06Sanitized");

        var messageBody = "{\"test\": \"hello\"}";
        var channel = getChannel();

        System.out.println("Do we have a channel?");
        System.out.println(channel);

        Assertions.assertNotNull(channel);


//        var smallEhrObservabilityQueueUrl = sqs.getQueueUrl(smallEhrObservabilityQueueName).getQueueUrl();
//
//        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
//            var receivedMessageHolder = checkMessageInRelatedQueue(smallEhrObservabilityQueueUrl);
//            Assertions.assertTrue(receivedMessageHolder.get(0).getBody().contains(smallEhrSanitized));
//            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
//            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
//        });
    }

    @Disabled("We need to create the byteMessage properly, possibly using the proton library")
    @Test
    void shouldPublishInvalidMessageToDlq() {
        var wrongMessage = "something wrong";

        jmsTemplate.send(inboundQueue, session -> {
            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(wrongMessage.getBytes(StandardCharsets.UTF_8));
            return bytesMessage;
        });

        var parsingDqlQueueUrl = sqs.getQueueUrl(parsingDlqQueueName).getQueueUrl();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(parsingDqlQueueUrl);
            Assertions.assertTrue(receivedMessageHolder.get(0).getBody().contains(wrongMessage));
        });
    }


    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
        System.out.println("checking sqs queue: " + queueUrl);

        var requestForMessagesWithAttributes
                = new ReceiveMessageRequest().withQueueUrl(queueUrl)
                .withMessageAttributeNames("All");
        var messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
        System.out.println("messages in checkMessageInRelatedQueue: " + messages);
        assertThat(messages).hasSize(1);
        return messages;
    }

    private void purgeQueue(String queueName) {
        var queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}