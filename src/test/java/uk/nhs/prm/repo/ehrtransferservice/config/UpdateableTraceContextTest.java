package uk.nhs.prm.repo.ehrtransferservice.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.nhs.prm.repo.ehrtransferservice.logging.UpdateableTraceContext;

import javax.jms.JMSException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.conversationId;
import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.traceId;

class UpdateableTraceContextTest {

    private static UpdateableTraceContext traceContext;

    @BeforeAll
    static void setUp() {
        traceContext = new UpdateableTraceContext();
    }

    @Test
    void updateTraceIdShouldAddTraceIdToLoggingContextWhenPassed() throws JMSException {
        traceContext.updateTraceId("someTraceId");

        String mdcTraceIdValue = MDC.get(traceId.toString());

        assertThat(mdcTraceIdValue).isEqualTo("someTraceId");
    }

    @Test
    void updateTraceIdShouldCreateAndAddHyphenatedUuidTraceIdWhenNotPresent() throws JMSException {
        traceContext.updateTraceId(null);

        String mdcTraceIdValue = MDC.get(traceId.toString());

        assertThat(mdcTraceIdValue).isNotNull();
        assertThat(UUID.fromString(mdcTraceIdValue)).isNotNull();
    }

    @Test
    void updateTraceIdShouldSetTheTraceIdInTheLoggingContext() {
        MDC.clear();

        traceContext.updateTraceId("bob");

        assertThat(MDC.get(traceId.toString())).isEqualTo("bob");
    }

    @Test
    void updateTraceIdShouldOverwriteTheTraceIdInTheLoggingContext() {
        MDC.put(traceId.toString(), "foo");

        traceContext.updateTraceId("bar");

        assertThat(MDC.get(traceId.toString())).isEqualTo("bar");
    }

    @Test
    void clearShouldClearTheConversationIdAndTraceIdWhenCleared() {
        MDC.put(traceId.toString(), "whoop");
        MDC.put(conversationId.toString(), "cheese");

        traceContext.clear();

        assertThat(MDC.get(conversationId.toString())).isNull();
        assertThat(MDC.get(traceId.toString())).isNull();
    }

    @Test
    void updateConversationIdShouldUpdateTheConversationIdButNotClearTheTraceId() {
        MDC.put(traceId.toString(), "some-trace-id");
        MDC.put(conversationId.toString(), "old-convo");

        traceContext.updateConversationId("new-convo");

        assertThat(MDC.get(traceId.toString())).isEqualTo("some-trace-id");
        assertThat(MDC.get(conversationId.toString())).isEqualTo("new-convo");
    }

    @Test
    void updateConversationIdShouldLeaveTheConversationIdIfNewOneBlank() {
        MDC.put(conversationId.toString(), "old-convo");

        traceContext.updateConversationId("");

        assertThat(MDC.get(conversationId.toString())).isEqualTo("old-convo");
    }

    @Test
    void updateConversationIdShouldLeaveTheConversationIdIfNewOneNull() {
        MDC.put(conversationId.toString(), "old-convo");

        traceContext.updateConversationId(null);

        assertThat(MDC.get(conversationId.toString())).isEqualTo("old-convo");
    }
}
