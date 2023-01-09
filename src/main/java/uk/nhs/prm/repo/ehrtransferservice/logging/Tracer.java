package uk.nhs.prm.repo.ehrtransferservice.logging;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        UpdateableTraceContext context = createNewContext();

        context.updateTraceId(message.getStringProperty(traceId.toString()));
        context.updateConversationId(message.getStringProperty(conversationId.toString()));
    }

    public UpdateableTraceContext createNewContext() {
        var context = new UpdateableTraceContext();
        context.clear();
        return context;
    }

    public String getTraceId() {
        return new ReadOnlyTraceContext().getTraceId();
    }

    // oops, coming in backdoor? - chat with team

    public void directlyUpdateTraceIdButNotConversationId(String traceId) {
        new UpdateableTraceContext().setTraceIdNotThroughUpdateTraceId(traceId);
    }

    public static void directlyRemoveTraceId() {
        MDC.remove(traceId.toString());
    }
}
