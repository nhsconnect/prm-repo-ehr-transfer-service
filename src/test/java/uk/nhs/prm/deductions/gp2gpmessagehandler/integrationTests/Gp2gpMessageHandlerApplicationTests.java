package uk.nhs.prm.deductions.gp2gpmessagehandler.integrationTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration") // perhaps we need other name for tests that interact with external systems
@SpringBootTest
class Gp2gpMessageHandlerApplicationTests {
	@Autowired
	JmsTemplate jmsTemplate;

	@Value("${activemq.inboundQueue}")
	private String inboundQueue;

	@Value("${activemq.outboundQueue}")
	private String outboundQueue;

	@Value("${activemq.unhandledQueue}")
	private String unhandledQueue;

    @Value("${gpToRepoUrl}")
    String gpToRepoUrl;

    @Value("${gpToRepoAuthKey}")
    String gpToRepoAuthKey;

    private TestDataLoader dataLoader = new TestDataLoader();

    @Test
    void shouldPassThroughMessagesForOldWorker() throws IOException, JMSException {
        String ehrRequest = dataLoader.getDataAsString("RCMR_IN010000UK05.xml");
        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(ehrRequest.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });

        jmsTemplate.setReceiveTimeout(5000);
        BytesMessage message = (BytesMessage) jmsTemplate.receive(outboundQueue);
        assertNotNull(message);

		byte[] allTheBytes = new byte[(int) message.getBodyLength()];
		message.readBytes(allTheBytes);
		String messageAsString = new String(allTheBytes, StandardCharsets.UTF_8);
        assertThat(messageAsString, equalTo(ehrRequest));
    }

    @Test
    void shouldSendMalformedMessagesToUnhandledQ() throws JMSException {
        String malformedMessage = "clearly not a GP2GP message";

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(malformedMessage.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });

		jmsTemplate.setReceiveTimeout(5000);
		BytesMessage message = (BytesMessage) jmsTemplate.receive(unhandledQueue);
		assertNotNull(message);

        byte[] allTheBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(allTheBytes);
        String messageAsString = new String(allTheBytes, StandardCharsets.UTF_8);
        assertThat(messageAsString, equalTo(malformedMessage));
	}
}
