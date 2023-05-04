package uk.nhs.prm.repo.ehrtransferservice.activemq;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.prm.repo.ehrtransferservice.ActiveMQTestConfig;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ActiveMQTestConfig.class, MessageSender.class })
public class ActiveMQIntegrationTest {

        @Autowired
        private MessageSender messageSender;

        @ClassRule
        public static EmbeddedActiveMQBroker embeddedBroker = new EmbeddedActiveMQBroker();

        @Test
        public void test1() {
            assertThat(2+2).isEqualTo(4);
        }

        @Test
        public void whenSendingMessage_thenCorrectQueueAndMessageText() throws JMSException {
                String queueName = "queue-2";
                String messageText = "Test message";

                messageSender.sendTextMessage(queueName, messageText);

                assertThat(1).isEqualTo(embeddedBroker.getMessageCount(queueName));
                TextMessage sentMessage = embeddedBroker.peekTextMessage(queueName);
                assertEquals(messageText, sentMessage.getText());
        }
}
