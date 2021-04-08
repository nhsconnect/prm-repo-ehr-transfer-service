package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.JMSException;

import static net.logstash.logback.argument.StructuredArguments.v;

/*
Generic handler to put messages on the outbound queue
*/
public abstract class JsQueueMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(JsQueueMessageHandler.class);

    private final JmsTemplate jmsTemplate;
    private String outboundQueue;

    public JsQueueMessageHandler(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            logger.info("Sending message to outbound queue", v("queue", outboundQueue));
            jmsTemplate.convertAndSend(outboundQueue, parsedMessage.getBytesMessage());
        } catch (JMSException e) {
            logger.error("Failed to send the message to outbound queue", e, v("queue", outboundQueue));
        }
    }
}
