package uk.nhs.prm.repo.ehrtransferservice.logging;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;
import javax.jms.Message;

import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.conversationId;
import static uk.nhs.prm.repo.ehrtransferservice.logging.TraceKey.traceId;

@Slf4j
@Configuration
@NoArgsConstructor
public class Tracer {

    public void setMDCContextFromSqs(Message message) throws JMSException {
        var context = new TraceContext();
        context.clear();

        context.handleTraceId(message.getStringProperty(traceId.toString()));
        context.handleConversationId(message.getStringProperty(conversationId.toString()));
    }

    public void setMDCContextFromMhsInbound(String traceId) {
        var context = new TraceContext();
        context.clear();
        handleTraceId(traceId);
    }

    private void handleTraceId(String traceId) {
        var context = new TraceContext();
        context.handleTraceId(traceId);
    }

    public void handleConversationId(String conversationId) {
        var context = new TraceContext();
        context.handleConversationId(conversationId);
    }

    public String getTraceId() {
        return new TraceContext().getTraceId();
    }

    public void setTraceId(String traceId) {
        new TraceContext().setTraceId(traceId);
    }
}
