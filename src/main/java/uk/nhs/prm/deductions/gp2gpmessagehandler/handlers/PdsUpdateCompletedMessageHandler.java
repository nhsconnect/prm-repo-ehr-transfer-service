package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/*
 Can handle PRPA_IN000202UK01 message
 */
@Service
public class PdsUpdateCompletedMessageHandler extends JsQueueMessageHandler {
    public PdsUpdateCompletedMessageHandler(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue){
        super(jmsTemplate, outboundQueue);
    }

    @Override
    public String getInteractionId() {
        return "PRPA_IN000202UK01";
    }
}
