package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*
 Tests JMS Consumer together with queues
 */
@Tag("unit")
public class JmsConsumerIntegrationTest {
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    @Mock
    ParsingDlqPublisher parsingDlqPublisher;

    @Mock
    Tracer tracer;

    @InjectMocks
    JmsConsumer jmsConsumer;
    private AutoCloseable closeable;
    private final TestDataLoader dataLoader = new TestDataLoader();

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(bytes);
        message.reset();
        return message;
    }

    // TODO: this test has to be revisited (maybe deleted), it relies on messageSanitizer to break,
    // and messageSanitizer is going to be retired soon.
    // Also, the test cases for parsingDlqPublish are already covered in JmsConsumerTest.
    @ParameterizedTest
    @ValueSource(strings = {
            "simpleTextMessage.txt",
            "RCMR_IN030000UK06WithoutInteractionId",
            "RCMR_IN030000UK06WithoutMessageHeader",
            "RCMR_IN030000UK06WithoutSoapHeader",
            "RCMR_IN030000UK06WithIncorrectInteractionId",
    })
    void shouldSendMessageToUnhandledQueue(String fileName) throws JMSException, IOException {
        byte[] bytes = dataLoader.getDataAsBytes(fileName);
        String expected = "<NOT-PARSED-YET>";

        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage(bytes);
        jmsConsumer.onMessage(bytesMessage, new HashMap<>());
        verify(parsingDlqPublisher, times(1)).sendMessage(expected);
    }
}
