package uk.nhs.prm.repo.ehrtransferservice.logging;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Slf4j
@Configuration
@NoArgsConstructor
public class UpdateableTraceContext extends ReadOnlyTraceContext {

    public void clear() {
        MDC.remove(TraceKey.traceId.toString());
        MDC.remove(TraceKey.conversationId.toString());
    }

    public void updateTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            log.info("The message has no trace ID attribute, we'll create and assign one");
            setTraceId(createRandomUUID());
        } else {
            setTraceId(traceId);
        }
    }

    public void updateConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            log.warn("The message has no conversation ID attribute");
        } else {
            setConversationId(conversationId);
        }
    }

    public void setTraceIdNotThroughUpdateTraceId(String traceId) {
        setTraceId(traceId);
    }

    private static void setTraceId(String traceId) {
        MDC.put(TraceKey.traceId.toString(), traceId);
    }

    private void setConversationId(String conversationId) {
        MDC.put(TraceKey.conversationId.toString(), conversationId);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
