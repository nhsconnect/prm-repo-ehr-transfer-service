package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/*
 Can handle RCMR_IN010000UK05 message
 */
@Service
public class EhrRequestMessageHandler extends JsQueueMessageHandler {
    public EhrRequestMessageHandler(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue){
        super(jmsTemplate, outboundQueue);
    }

    @Override
    public String getInteractionId() {
        return "RCMR_IN010000UK05";
    }
}
