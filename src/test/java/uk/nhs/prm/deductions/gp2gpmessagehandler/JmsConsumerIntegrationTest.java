package uk.nhs.prm.deductions.gp2gpmessagehandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
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

    private ActiveMQBytesMessage getActiveMQBytesMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(bytes);
        message.reset();
        return message;
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
        byte[] bytes = dataLoader.getDataAsBytes(fileName);
        ActiveMQBytesMessage message = getActiveMQBytesMessage(bytes);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, times(1)).convertAndSend(ArgumentMatchers.eq(unhandledQueue), ArgumentMatchers.eq(message));
    }
}
