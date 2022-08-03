package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    private ByteBuffer magicCodeCopiedFromActiveMQCodebaseItself(org.apache.qpid.proton.message.Message message) {
        ProtonJMessage amqp = (ProtonJMessage) message;

        ByteBuffer buffer = ByteBuffer.wrap(new byte[1024 * 4]);
        final DroppingWritableBuffer overflow = new DroppingWritableBuffer();
        int c = amqp.encode(new CompositeWritableBuffer(new WritableBuffer.ByteBufferWrapper(buffer), overflow));
        if (overflow.position() > 0) {
            buffer = ByteBuffer.wrap(new byte[1024 * 4 + overflow.position()]);
            c = amqp.encode(new WritableBuffer.ByteBufferWrapper(buffer));
        }

        return buffer;
//        return new EncodedMessage(1, buffer.array(), 0, c);
    }

    String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<RCMR_IN030000UK06 xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:hl7-org:v3 ..SchemasRCMR_IN030000UK06.xsd\">\n" +
            "    <ControlActEvent classCode=\"CACT\" moodCode=\"EVN\">\n" +
            "        <subject typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
            "            <EhrExtract classCode=\"EXTRACT\" moodCode=\"EVN\">\n" +
            "                <recordTarget typeCode=\"RCT\">\n" +
            "                    <patient classCode=\"PAT\">\n" +
            "                        <id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9442964410\" />\n" +
            "                    </patient>" +
            "                </recordTarget>" +
            "             </EhrExtract>" +
            "         </subject>" +
            "    </ControlActEvent>" +
            "</RCMR_IN030000UK06>";

    @Disabled("WIP - building message using proton libraries")
    @Test
    void shouldPublishSmallMessageToSmallEhrObservabilityQueue() throws IOException {
        var smallEhr = dataLoader.getDataAsString("RCMR_IN030000UK06");
        var smallEhrSanitized = dataLoader.getDataAsString("RCMR_IN030000UK06Sanitized");

        jmsTemplate.send(inboundQueue, session -> {
            // THIS GETS PARSED
//            var originalString = "{\"test\": \"hello\"}";

            // THIS DOES NOT, it can't get past amqpMessage.decode in Parser.java
//            var originalString = smallEhrSanitized; // THIS DOES NOT

            var json = new JsonObject();
            json.addProperty("ebXML", extract);
            // THIS GET PARSED, then it fails in the creation of ParseMessage
            // to verify if there are all needed fields in it
            var originalString = new Gson().toJson(json);

            System.out.println("original message is");
            System.out.println(originalString);

            var body = new AmqpValue(originalString);
            var message = Factory.create();
            message.setBody(body);
            message.setContentType("application/json");
            message.setMessageId(UUID.randomUUID());
//            message.setContentEncoding("UTF-8"); // this break what currently works
            var buffer = magicCodeCopiedFromActiveMQCodebaseItself(message);

            var writableBuffer = new WritableBuffer.ByteBufferWrapper(buffer);

            message.encode(writableBuffer);

            var bytes = writableBuffer.toReadableBuffer().array();
//            var backToString = new String(bytes, StandardCharsets.UTF_8);
//            System.out.println("Was it converted back to its original value via MAGIC?");
//            System.out.println(backToString);
//            System.out.println( "<-------------");

            var bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(bytes);
            return bytesMessage;
//            bytesMessage.writeBytes(smallEhr.getBytes(StandardCharsets.UTF_8));
//            return bytesMessage;
        });

        var smallEhrObservabilityQueueUrl = sqs.getQueueUrl(smallEhrObservabilityQueueName).getQueueUrl();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var receivedMessageHolder = checkMessageInRelatedQueue(smallEhrObservabilityQueueUrl);
            Assertions.assertTrue(receivedMessageHolder.get(0).getBody().contains(smallEhrSanitized));
//            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("traceId"));
//            Assertions.assertTrue(receivedMessageHolder.get(0).getMessageAttributes().containsKey("conversationId"));
        });
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