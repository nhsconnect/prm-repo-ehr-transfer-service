package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class SmallEhrMessageListener implements MessageListener {

    private final Tracer tracer;
    private final Parser parser;
    private final SmallEhrMessageHandler smallEhrMessageHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from small-ehr queue");
            String payload = ((TextMessage) message).getText();
            var parsedMessage = parser.parse(payload);
            smallEhrMessageHandler.handleMessage(parsedMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from small-ehr queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

}
