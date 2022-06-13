package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.handlers.NegativeAcknowledgementHandler;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
@Component
public class NegativeAcknowledgementListener implements MessageListener {

    private final NegativeAcknowledgementHandler handler;

    @Override
    public void onMessage(Message message) {
        try {
            handler.handleMessage( ((TextMessage) message).getText());
            message.acknowledge();
        } catch (Exception e) {
            log.error("Error while processing negative acknowledgement", e);
        }
    }
}
