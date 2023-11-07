package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@Tag("unit")
class JmsProducerTest {
    @Mock
    JmsTemplate jmsTemplate;
    @InjectMocks
    JmsProducer jmsProducer;
    @Captor
    ArgumentCaptor<ActiveMQBytesMessage> bytesMessageArgumentCaptor;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

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
