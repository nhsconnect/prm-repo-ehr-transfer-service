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
    public static final String NEMS_MESSAGE_ID = "nemsMessageId";
    public static final String CONVERSATION_ID = "conversationId";

    public void setMDCContext(Message message) throws JMSException {
        clearMDCContext();
        handleTraceId(message);
        handleNemsMessageId(message);
        handleConversationId(message.getStringProperty(CONVERSATION_ID));
    }

    private void handleTraceId(Message message) throws JMSException {
        if (message.getStringProperty(TRACE_ID) == null || message.getStringProperty(TRACE_ID).isBlank()) {
            log.info("The message has no trace ID attribute, we'll create and assign one.");
            setTraceId(createRandomUUID());
        } else {
            setTraceId(message.getStringProperty(TRACE_ID));
        }
    }

    public void setMDCContextFromMhsInbound(String conversationId) {
        clearMDCContext();
        handleTraceIdFromMhsInbound(conversationId);
        handleConversationId(conversationId);
    }

    private void handleTraceIdFromMhsInbound(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            log.error("The message received on mhs inbound has no trace ID attribute, we'll create and assign one.");
            setTraceId(createRandomUUID());
        } else {
            setTraceId(traceId);
        }
    }

    public String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private void handleConversationId(String conversationId) {
        if (conversationId == null) {
            log.error("The message has no conversation ID attribute.");
        } else {
            setConversationId(conversationId);
        }
    }

    public String getConversationId() {
        return MDC.get(CONVERSATION_ID);
    }

    private void setConversationId(String conversationId) {
        MDC.put(CONVERSATION_ID, conversationId);
    }

    private void handleNemsMessageId(Message message) throws JMSException {
        if (message.getStringProperty(NEMS_MESSAGE_ID) == null) {
            log.warn("The message has no NEMS message ID attribute");
        } else {
            setNemsMessageId(message.getStringProperty(NEMS_MESSAGE_ID));
        }
    }

    public String getNemsMessageId() {
        return MDC.get(NEMS_MESSAGE_ID);
    }

    private void setNemsMessageId(String nemsMessageId) {
        MDC.put(NEMS_MESSAGE_ID, nemsMessageId);
    }

    private void clearMDCContext() {
        MDC.remove(TRACE_ID);
        MDC.remove(NEMS_MESSAGE_ID);
        MDC.remove(CONVERSATION_ID);
    }
}
