package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class LargeEhrMessageListener implements MessageListener {
    private final Tracer tracer;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message);
            log.info("RECEIVED: Message from large-ehr queue");
            String payload = ((TextMessage) message).getText();
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from large-ehr queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }
}
