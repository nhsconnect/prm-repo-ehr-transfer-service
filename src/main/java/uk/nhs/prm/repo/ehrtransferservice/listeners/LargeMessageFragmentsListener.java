package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeMessageFragmentHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.LargeSqsMessageParser;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LargeMessageFragmentsListener implements MessageListener {
    private final Tracer tracer;
    private final LargeSqsMessageParser largeSqsMessageParser;
    private final LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContextFromSqs(message);
            log.info("RECEIVED: Message from Large Message Fragments queue");
            largeMessageFragmentHandler.handleMessage(getLargeMessageFragment(message));
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from Large Message Fragments queue");
        } catch (Exception e) {
            log.error("Error while processing message from Large Message Fragments queue", e);
        }
    }

    private LargeSqsMessage getLargeMessageFragment(Message message) throws IOException, JMSException {
        String payload = ((TextMessage) message).getText();
        return largeSqsMessageParser.getLargeSqsMessage(payload);
    }
}
