package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeEhrCoreMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3PointerMessageFetcher;

import javax.jms.Message;
import javax.jms.MessageListener;

@Slf4j
@RequiredArgsConstructor
public class LargeEhrMessageListener implements MessageListener {
    private final Tracer tracer;
    private final S3PointerMessageFetcher s3PointerMessageFetcher;
    private final LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from large-ehr queue");
            var largeEhrMessage = s3PointerMessageFetcher.parse(message);
            largeEhrCoreMessageHandler.handleMessage(largeEhrMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from large-ehr queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }
}
