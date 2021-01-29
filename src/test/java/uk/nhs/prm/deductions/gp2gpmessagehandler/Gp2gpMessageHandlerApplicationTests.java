package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
class Gp2gpMessageHandlerApplicationTests {
	@Autowired
	JmsTemplate jmsTemplate;

	@Value("${activemq.inboundQueue}")
	private String inboundQueue;

	@Test
	void contextLoads() {
		//action: send a message on the inbound q
		jmsTemplate.convertAndSend(inboundQueue,"test message");

		//assertion: verify the message gets on the outbound q
	}

}
