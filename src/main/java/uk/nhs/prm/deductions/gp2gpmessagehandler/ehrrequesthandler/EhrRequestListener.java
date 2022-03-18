package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.deductions.gp2gpmessagehandler.config.Tracer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EhrRequestListener implements MessageListener {

    private final Tracer tracer;
    private final ConversationIdGenerator conversationIdGenerator;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message,generateConversationId());
            log.info("RECEIVED: Message from RepoIncoming");
            String payload = ((TextMessage) message).getText();
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from RepoIncoming");
        } catch (Exception e) {
            log.error("Error while processing message: {}", e.getMessage());
        }
    }

    private String generateConversationId() {
        conversationIdGenerator.setConversationId(UUID.randomUUID().toString());
        return conversationIdGenerator.getConversationId();
    }
}
