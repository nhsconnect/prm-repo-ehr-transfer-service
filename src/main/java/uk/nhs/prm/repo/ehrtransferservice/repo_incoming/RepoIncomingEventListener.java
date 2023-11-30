package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class RepoIncomingEventListener implements MessageListener {
    private final Tracer tracer;
    private final RepoIncomingService repoIncomingService;
    private final RepoIncomingEventParser parser;

    @Value("${processingPeriodMilliseconds}")
    private int processingPeriodMilliseconds;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from RepoIncoming");
            RepoIncomingEvent parsedMessage = parseMessage(message);
            repoIncomingService.processIncomingEvent(parsedMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from RepoIncoming");
        } catch (Exception exception) {
            log.error("Error while processing message", exception);
        }
        waitForEmisProcessingPeriod();
    }

    private RepoIncomingEvent parseMessage(Message message) throws JMSException {
        String payload = ((TextMessage) message).getText();
        RepoIncomingEvent parsedMessage = parser.parse(payload);
        log.info("PARSED: message with conversationId " + parsedMessage.getConversationId());
        return parsedMessage;
    }

    private void waitForEmisProcessingPeriod() {
        try {
            Thread.sleep(processingPeriodMilliseconds);
        } catch (InterruptedException e) {
            log.error("Caught interruptedException waiting for EMIS processing period", e);
        }
    }
}
