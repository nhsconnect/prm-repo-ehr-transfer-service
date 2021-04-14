package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import static net.logstash.logback.argument.StructuredArguments.v;

/*
Generic handler to put messages on the outbound queue
*/
public abstract class JsQueueMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(JsQueueMessageHandler.class);

    private final JmsProducer jmsProducer;
    private String outboundQueue;

    public JsQueueMessageHandler(JmsProducer jmsProducer, @Value("${activemq.outboundQueue}") String outboundQueue) {
        this.jmsProducer = jmsProducer;
        this.outboundQueue = outboundQueue;
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        logger.info("Sending message to outbound queue", v("queue", outboundQueue));
        jmsProducer.sendMessageToQueue(outboundQueue, parsedMessage.getRawMessage());
    }
}
