package uk.nhs.prm.repo.ehrtransferservice.logging;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.traceId;

@Slf4j
@Configuration
@NoArgsConstructor
public class TraceContext {

    public void clear() {
        MDC.remove(TraceKey.traceId.toString());
        MDC.remove(TraceKey.conversationId.toString());
    }

    public void handleTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            log.info("The message has no trace ID attribute, we'll create and assign one");
            setTraceId(createRandomUUID());
        } else {
            setTraceId(traceId);
        }
    }

    public void handleConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            log.warn("The message has no conversation ID attribute");
        } else {
            setConversationId(conversationId);
        }
    }

    public String getTraceId() {
        return MDC.get(traceId.toString());
    }

    public void setTraceId(String traceId) {
        MDC.put(TraceKey.traceId.toString(), traceId);
    }

    public void setConversationId(String conversationId) {
        MDC.put(TraceKey.conversationId.toString(), conversationId);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
