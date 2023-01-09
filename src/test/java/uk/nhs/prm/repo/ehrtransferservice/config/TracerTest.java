package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;

import javax.jms.JMSException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.conversationId;
import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.traceId;

class TracerTest {

    private static final String SOME_NEMS_ID = "someNemsId";
    private static final String SOME_TRACE_ID = "someTraceId";
    private static final String SOME_CONVERSATION_ID = "someConversationId";
    private static Tracer tracer;

    public static final String TRACE_ID = traceId.toString();
    public static final String CONVERSATION_ID = conversationId.toString();

    @BeforeAll
    static void setUp() {
        tracer = new Tracer();
    }

    @Test
    void shouldAddTraceIdToMDCWhenItIsPresentInMessage() throws JMSException {
        SQSTextMessage message = spy(new SQSTextMessage("payload"));
        message.setStringProperty(TRACE_ID, SOME_TRACE_ID);
        message.setStringProperty(CONVERSATION_ID, SOME_CONVERSATION_ID);

        tracer.setMDCContextFromSqs(message);
        String mdcTraceIdValue = MDC.get(TRACE_ID);
        String mdcConversationIdValue = MDC.get(CONVERSATION_ID);
        assertThat(mdcTraceIdValue).isEqualTo(SOME_TRACE_ID);
        assertThat(mdcConversationIdValue).isEqualTo(SOME_CONVERSATION_ID);
    }

    @Test
    void shouldCreateAndAddHyphenatedUuidTraceIdToMDCWhenItIsNotPresentInMessage() throws JMSException {
        SQSTextMessage message = spy(new SQSTextMessage("payload"));

        tracer.setMDCContextFromSqs(message);

        String mdcTraceIdValue = MDC.get(TRACE_ID);
        assertThat(mdcTraceIdValue).isNotNull();
        assertThat(UUID.fromString(mdcTraceIdValue)).isNotNull();
    }

    @Test
    void shouldSetTheTraceIdInTheLoggingContextWhenCallingWithTraceIdFromMhsInbound() {
        MDC.clear();

        tracer.setMDCContextFromMhsInbound("bob");

        assertThat(MDC.get(traceId.toString())).isEqualTo("bob");
    }

    @Test
    void shouldOverwriteTheTraceIdInTheLoggingContextWhenCallingFromMhsInbound() {
        MDC.put(TRACE_ID, "foo");

        tracer.setMDCContextFromMhsInbound("bar");

        assertThat(MDC.get(traceId.toString())).isEqualTo("bar");
    }

    @Test
    void shouldClearTheConversationIdFromTheLoggingContextWhenCallingFromMhsInbound() {
        MDC.put(CONVERSATION_ID, "cheese");

        tracer.setMDCContextFromMhsInbound("whatevs");

        assertThat(MDC.get(conversationId.toString())).isNull();
    }

    @Test
    void shouldUpdateTheConversationIdButNotClearTheTraceIdWhenCallingHandleConversationId() {
        MDC.put(TRACE_ID, "some-trace-id");
        MDC.put(CONVERSATION_ID, "old-convo");

        tracer.handleConversationId("new-convo");

        assertThat(MDC.get(traceId.toString())).isEqualTo("some-trace-id");
        assertThat(MDC.get(conversationId.toString())).isEqualTo("new-convo");
    }

    @Test
    void shouldLeaveTheConversationIdIfNewOneBlankWhenCallingHandleConversationId() {
        MDC.put(CONVERSATION_ID, "old-convo");

        tracer.handleConversationId("");

        assertThat(MDC.get(conversationId.toString())).isEqualTo("old-convo");
    }

    @Test
    void shouldLeaveTheConversationIdIfNewOneNullWhenCallingHandleConversationId() {
        MDC.put(CONVERSATION_ID, "old-convo");

        tracer.handleConversationId(null);

        assertThat(MDC.get(conversationId.toString())).isEqualTo("old-convo");
    }
}
