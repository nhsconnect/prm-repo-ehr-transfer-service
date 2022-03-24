package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EhrRequestListener implements MessageListener {

    private final Tracer tracer;
    private final ConversationIdGenerator conversationIdGenerator;
    private final EhrRequestService ehrRequestService;

    @Override
    public void onMessage(Message message) {
        try {
            var conversationId = generateConversationId();
            tracer.setMDCContext(message, conversationId);
            log.info("RECEIVED: Message from RepoIncoming");
            String payload = ((TextMessage) message).getText();
            ehrRequestService.processIncomingEvent(payload);
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
