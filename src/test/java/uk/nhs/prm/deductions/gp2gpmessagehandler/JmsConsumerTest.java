package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.jms.JMSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {

    @Mock
    JmsTemplate mockJmsTemplate;

    private TestDataLoader dataLoader = new TestDataLoader();

    private ActiveMQBytesMessage getActiveMQBytesMessage(String content) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(content.getBytes(StandardCharsets.UTF_8));
        message.reset();
        return message;
    }

    private void jmsConsumerTestFactory(String fileName, String expectedQueue) throws IOException, JMSException {
        String ehrRequest = dataLoader.getData(fileName);
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(ehrRequest);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend(expectedQueue, message);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ehrRequestRCMR_IN010000UK05InteractionId.xml",
            "ehrRequestRCMR_IN030000UK06InteractionId.xml",
            "ehrRequestPRPA_IN000202UK01InteractionId.xml"
    })
    void shouldSendMessageWithKnownInteractionIdsToOutboundQueue(String fileName) throws JMSException, IOException {
        jmsConsumerTestFactory(fileName, "outbound");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simpleTextMessage.txt",
            "nonSoapMimeMessage.xml",
            "ehrRequestWithoutInteractionId.xml",
            "ehrRequestWithoutMessageHeader.xml",
            "ehrRequestWithoutSoapHeader.xml",
            "ehrRequestIncorrectInteractionId.xml"
    })
    void shouldSendMessageToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("simpleTextMessage.txt", "unhandled");
    }
}
