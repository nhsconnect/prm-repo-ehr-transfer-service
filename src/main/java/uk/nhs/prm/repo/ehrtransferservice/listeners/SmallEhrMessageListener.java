package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;

import javax.jms.Message;
import javax.jms.MessageListener;

@Slf4j
@RequiredArgsConstructor
public class SmallEhrMessageListener implements MessageListener {

    private final Tracer tracer;
    private final Parser parser;
    private final SmallEhrMessageHandler smallEhrMessageHandler;
    private final S3ExtendedMessageFetcher extendedMessageFetcher;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from small-ehr queue");
            var parsedMessage = extendedMessageFetcher.fetchAndParse(message);
            smallEhrMessageHandler.handleMessage(parsedMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from small-ehr queue");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

}
