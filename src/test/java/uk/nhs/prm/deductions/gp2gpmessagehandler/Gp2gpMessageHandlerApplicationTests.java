package uk.nhs.prm.deductions.gp2gpmessagehandler;

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

	private TestDataLoader dataLoader = new TestDataLoader();

	@Test
	void shouldPassThroughMessagesForOldWorker() throws IOException {
		String ehrRequest = dataLoader.getData("ehrRequestSoapEnvelope.xml");
		//action: send a message on the inbound q
		jmsTemplate.send(inboundQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				BytesMessage bytesMessage = session.createBytesMessage();
				bytesMessage.writeBytes(ehrRequest.getBytes(StandardCharsets.UTF_8));
				return bytesMessage;
			}
		});

		//assertion: verify the message gets on the outbound q
		jmsTemplate.setReceiveTimeout(5000);
		BytesMessage message = (BytesMessage) jmsTemplate.receive(outboundQueue);
		assertNotNull(message);
		try {
			String stringMessage = message.readUTF();
			assertThat(stringMessage, equalTo(ehrRequest));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Test
	void shouldSendMalformedMessagesToUnhandledQ() {
		String malformedMessage = "clearly not a GP2GP message";

		jmsTemplate.send(inboundQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				BytesMessage bytesMessage = session.createBytesMessage();
				bytesMessage.writeUTF(malformedMessage);
				return bytesMessage;
			}
		});

		//assertion: verify the message gets on the outbound q
		jmsTemplate.setReceiveTimeout(5000);
		BytesMessage message = (BytesMessage) jmsTemplate.receive(unhandledQueue);
		assertNotNull(message);
		try {
			String stringMessage = message.readUTF();
			assertThat(stringMessage, equalTo(malformedMessage));

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
