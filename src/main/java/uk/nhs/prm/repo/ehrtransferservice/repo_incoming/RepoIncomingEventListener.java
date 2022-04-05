package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RepoIncomingEventListener implements MessageListener {

    private final Tracer tracer;
    private final ConversationIdStore conversationIdStore;
    private final RepoIncomingService repoIncomingService;
    private final RepoIncomingEventParser parser;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message,  generateAndSetConversationId());
            log.info("RECEIVED: Message from RepoIncoming");
            processMessage(message);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from RepoIncoming");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

    private void processMessage(Message message) throws Exception {
        String payload = ((TextMessage)message).getText();
        var parsedMessage = parser.parse(payload);
        repoIncomingService.processIncomingEvent(parsedMessage);
    }

    private String generateAndSetConversationId() {
        conversationIdStore.setConversationId(UUID.randomUUID().toString());
        return conversationIdStore.getConversationId();
    }
}
