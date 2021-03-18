package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.BytesMessage;

import static net.logstash.logback.argument.StructuredArguments.v;

/*
 Can handle gp2gp message: RCMR_IN030000UK06
 */
@Service
public class EhrExtractMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private final JmsTemplate jmsTemplate;
    private String outboundQueue;

    public EhrExtractMessageHandler(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
    }

    @Override
    public String getInteractionId() {
        return "RCMR_IN030000UK06";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage, BytesMessage bytesMessage) {
        if (parsedMessage.isLargeMessage()) {
            //store message
            //send continue message
        } else {
            logger.info("Sending message to outbound queue", v("queue", outboundQueue));
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        }
    }
}
