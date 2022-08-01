package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeMessageFragmentHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;

import javax.jms.Message;
import javax.jms.MessageListener;

@Slf4j
@RequiredArgsConstructor
public class LargeMessageFragmentsListener implements MessageListener {
    private final Tracer tracer;
    private final S3ExtendedMessageFetcher s3ExtendedMessageFetcher;
    private final LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from Large Message Fragments queue");
            var largeEhrMessage = s3ExtendedMessageFetcher.fetchAndParse(message);
            largeMessageFragmentHandler.handleMessage(largeEhrMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from Large Message Fragments queue");
        } catch (Exception e) {
            log.error("Error while processing message from Large Message Fragments queue", e);
        }
    }
}
