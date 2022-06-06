package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LargeEhrMessageListener implements MessageListener {
    private final Tracer tracer;
    private final S3PointerMessageHandler s3PointerMessageHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message);
            log.info("RECEIVED: Message from large-ehr queue");
            var largeEhrMessage = getLargeEhrMessage(message);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from large-ehr queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

    private LargeEhrMessage getLargeEhrMessage(Message message) throws IOException, JMSException {
        String payload = ((TextMessage) message).getText();
        return s3PointerMessageHandler.getLargeMessage(payload);
    }
}
