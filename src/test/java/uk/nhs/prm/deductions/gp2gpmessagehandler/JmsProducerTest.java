package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.*;
import javax.jms.JMSException;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
public class JmsProducerTest {
    JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    JmsProducer jmsProducer = new JmsProducer(jmsTemplate);

    @BeforeEach
    public void setUp() {
        // FIXME: find a non-deprecated way of initialising the ArgumentCaptor
        MockitoAnnotations.initMocks(this);
    }

    @Captor
    ArgumentCaptor<ActiveMQBytesMessage> bytesMessageArgumentCaptor;

    @Test
    void shouldSendMessageToQueue() throws JMSException {
        String expectedMessage = "test";
        String queueName = "test-queue";

        jmsProducer.sendMessageToQueue(queueName, expectedMessage);
        verify(jmsTemplate).convertAndSend(eq(queueName), bytesMessageArgumentCaptor.capture());

        ActiveMQBytesMessage bytesMessage = bytesMessageArgumentCaptor.getValue();
        byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(contentAsBytes);
        String actualMessage = new String(contentAsBytes, StandardCharsets.UTF_8);

        assertEquals(expectedMessage, actualMessage);
    }
}
