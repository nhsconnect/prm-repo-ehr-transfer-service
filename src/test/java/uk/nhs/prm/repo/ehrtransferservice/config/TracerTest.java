package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import javax.jms.JMSException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.nhs.prm.repo.ehrtransferservice.config.Tracer.*;

class TracerTest {

    private static final String SOME_NEMS_ID = "someNemsId";
    private static final String SOME_TRACE_ID = "someTraceId";
    private static final String SOME_CONVERSATION_ID = "someConversationId";
    private static Tracer tracer;

    @BeforeAll
    static void setUp() {
        tracer = new Tracer();
    }

    @Test
    void shouldAddTraceIdToMDCWhenItIsPresentInMessage() throws JMSException {
        SQSTextMessage message = spy(new SQSTextMessage("payload"));
        message.setStringProperty(TRACE_ID, SOME_TRACE_ID);

        tracer.setMDCContext(message, SOME_CONVERSATION_ID);
        String mdcTraceIdValue = MDC.get(TRACE_ID);
        String mdcConversationIdValue = MDC.get(CONVERSATION_ID);
        assertThat(mdcTraceIdValue).isEqualTo(SOME_TRACE_ID);
        assertThat(mdcConversationIdValue).isEqualTo(SOME_CONVERSATION_ID);
    }

    @Test
    void shouldCreateAndAddHyphenatedUuidTraceIdToMDCWhenItIsNotPresentInMessage() throws JMSException {
        SQSTextMessage message = spy(new SQSTextMessage("payload"));

        tracer.setMDCContext(message, SOME_CONVERSATION_ID);
        String mdcTraceIdValue = MDC.get(TRACE_ID);
        assertThat(mdcTraceIdValue).isNotNull();
        assertThat(UUID.fromString(mdcTraceIdValue)).isNotNull();
    }

    @Test
    void shouldAddNemsMessageIdToMDC() throws JMSException {
        SQSTextMessage message = spy(new SQSTextMessage("payload"));
        message.setStringProperty(NEMS_MESSAGE_ID, SOME_NEMS_ID);

        tracer.setMDCContext(message, SOME_CONVERSATION_ID);
        String mdcValue = MDC.get(NEMS_MESSAGE_ID);
        assertThat(mdcValue).isEqualTo(SOME_NEMS_ID);
    }
}