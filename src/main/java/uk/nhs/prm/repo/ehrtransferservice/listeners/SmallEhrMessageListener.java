package uk.nhs.prm.repo.ehrtransferservice.listeners;

import lombok.RequiredArgsConstructor;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SmallEhrMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SmallEhrMessageListener implements MessageListener {

    private final Tracer tracer;
    private final Parser parser;
    SmallEhrMessageHandler smallEhrMessageHandler;

    @Override
    public void onMessage(Message message) {
        try {
            tracer.setMDCContext(message);
            log.info("RECEIVED: Message from small-ehr");
            String payload = ((TextMessage) message).getText();
            var parsedMessage = parser.parse(payload);
            smallEhrMessageHandler.handleMessage(parsedMessage);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Message from small-ehr");
        } catch (Exception e) {
            log.error("Error while processing message", e);
        }
    }

}
