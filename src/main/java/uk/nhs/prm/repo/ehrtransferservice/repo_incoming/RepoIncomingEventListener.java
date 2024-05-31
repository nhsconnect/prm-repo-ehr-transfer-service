package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
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

    // time gap between our EHR requests to allow Foundation Suppliers to process other requests they might receive
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
            // TODO PRMT-4840 build this out. I think a private method is more appropriate here so we can process
            //  different exceptions that require acknowledging the message such as PRMT-4859's EhrCompleteAcknowledgementFailedException
        } catch (ConversationIneligibleForRetryException exception) {
            try {
                message.acknowledge();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
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
