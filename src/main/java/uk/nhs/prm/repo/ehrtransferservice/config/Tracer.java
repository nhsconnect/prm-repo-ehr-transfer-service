package uk.nhs.prm.repo.ehrtransferservice.config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.UUID;

@Slf4j
@Configuration
@NoArgsConstructor
public class Tracer {
    public static final String TRACE_ID = "traceId";
    public static final String CONVERSATION_ID = "conversationId";

    public void setMDCContextFromSqs(Message message) throws JMSException {
        clearMDCContext();
        handleTraceId(message.getStringProperty(TRACE_ID));
        handleConversationId(message.getStringProperty(CONVERSATION_ID));
    }

    public void setMDCContextFromMhsInbound(String traceId) {
        clearMDCContext();
        handleTraceId(traceId);
    }

    private void handleTraceId(String traceId) {
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
        return MDC.get(TRACE_ID);
    }

    public void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    private void setConversationId(String conversationId) {
        MDC.put(CONVERSATION_ID, conversationId);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private void clearMDCContext() {
        MDC.remove(TRACE_ID);
        MDC.remove(CONVERSATION_ID);
    }
}
