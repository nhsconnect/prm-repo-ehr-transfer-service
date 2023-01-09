package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrCompleteHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.EhrCompleteParser;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class EhrCompleteMessageListener implements MessageListener {

    private final Tracer tracer;
    private final EhrCompleteParser parser;
    private final EhrCompleteHandler ehrCompleteHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from ehr-complete queue");
            String payload = ((TextMessage) message).getText();
            var parsedMessage = parser.parse(payload);
            ehrCompleteHandler.handleMessage(parsedMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from ehr-complete queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }
}
