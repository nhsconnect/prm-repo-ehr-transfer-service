package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class EhrCompleteMessageListener implements MessageListener {

    private final Tracer tracer;
    private final Parser parser;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message);
            log.info("RECEIVED: Message from ehr-complete topic");
            String payload = ((TextMessage) message).getText();
            parser.parse(payload);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from ehr-complete topic");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

}
