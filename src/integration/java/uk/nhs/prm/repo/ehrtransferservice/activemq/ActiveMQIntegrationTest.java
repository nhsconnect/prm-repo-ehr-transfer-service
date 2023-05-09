package uk.nhs.prm.repo.ehrtransferservice.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.prm.repo.ehrtransferservice.ActiveMQTestConfig;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {ActiveMQTestConfig.class, MessageSender.class})
public class ActiveMQIntegrationTest {
//    @Spy
//    private MessageListener messageListener;
//
    @Autowired
    private MessageSender messageSender;
//
    @ClassRule
    public static final EmbeddedActiveMQBroker BROKER = new EmbeddedActiveMQBroker();

    private static final String ON_QUEUE_ONE = "queue-1";

    private static final String ON_QUEUE_TWO = "queue-2";

    private static final String CONNECTION_URI = "tcp://localhost:61616";
    private Connection connection;
    private Destination destination;
    private Session session;
    private BrokerService service;

    @Test
    public void whenSendingMessage_thenCorrectQueueAndMessageText() throws JMSException {
        // Given
        String messageText = "Test message";

        // When
        BROKER.start();
        messageSender.sendTextMessage(ON_QUEUE_TWO, messageText);
        TextMessage sentMessage = BROKER.peekTextMessage(ON_QUEUE_TWO);

        // Then
        assertThat(BROKER.getMessageCount(ON_QUEUE_TWO)).isEqualTo(1);
        assertEquals(messageText, sentMessage.getText());
    }
//
////    @Test
////    public void testAcknowledgeMode() throws JMSException {
////        // Given
////        String messageText = "Test message";
////
////        // When
////        broker.start();
////        messageSender.sendTextMessage(QUEUE_TWO, messageText);
////        TextMessage sentMessage = broker.peekTextMessage(QUEUE_TWO);
////
////        // When
////        assertThat(broker.getMessageCount(QUEUE_TWO)).isEqualTo(1);
////        assertEquals(messageText, sentMessage.getText());
////    }
//
    @Test
    public void whenListening_thenReceivingCorrectMessage() throws JMSException {
        // Given
        String messageText = "Test message";

        // When
        BROKER.start();
        BROKER.pushMessage(ON_QUEUE_ONE, messageText);

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        Mockito.verify(messageListener, Mockito.timeout(100)).sampleJmsListenerMethod(messageCaptor.capture());
        TextMessage receivedMessage = messageCaptor.getValue();
        assertEquals(messageText, receivedMessage.getText());
        assertEquals(1, BROKER.getMessageCount(ON_QUEUE_ONE));
    }
}
