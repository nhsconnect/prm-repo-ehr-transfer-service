package uk.nhs.prm.deductions.gp2gpmessagehandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrExtractMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrRequestMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.PdsUpdateCompletedMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.jms.JMSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

/*
 Tests JMS Consumer together with other classes but without talking to a real queue server
 */
@Tag("unit")
@SpringBootTest(classes = { JmsConsumer.class, MessageSanitizer.class, ParserService.class,
        EhrExtractMessageHandler.class, PdsUpdateCompletedMessageHandler.class, EhrRequestMessageHandler.class })
public class JmsConsumerIntegrationTest {
    @Autowired
    JmsConsumer jmsConsumer;

    @MockBean
    JmsTemplate mockJmsTemplate;

    @MockBean
    GPToRepoClient gpToRepoClient;

    @MockBean
    EhrRepoService ehrRepoService;

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    private TestDataLoader dataLoader = new TestDataLoader();

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD")
    private ActiveMQBytesMessage getActiveMQBytesMessage(String content) throws JMSException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return getActiveMQBytesMessage(bytes);
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(bytes);
        message.reset();
        return message;
    }

    private void jmsConsumerTestFactory(String fileName, String expectedQueue) throws IOException, JMSException {
        byte[] bytes = dataLoader.getDataAsBytes(fileName);
        ActiveMQBytesMessage message = getActiveMQBytesMessage(bytes);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, times(1)).convertAndSend(ArgumentMatchers.eq(expectedQueue), ArgumentMatchers.eq(message));
    }

    @ParameterizedTest
    @ValueSource(strings = {
//            "RCMR_IN010000UK05.xml",
//            "RCMR_IN030000UK06.xml",
//            "PRPA_IN000202UK01.xml",
            "tppSmallEhr.xml"
    })
    void shouldSendMessageWithKnownInteractionIdsToOutboundQueue(String fileName) throws JMSException, IOException {
        jmsConsumerTestFactory(fileName, outboundQueue);
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
    void shouldSendMessageToUnhandledQueue(String fileName) throws JMSException, IOException {
        jmsConsumerTestFactory(fileName, unhandledQueue);
    }
}
