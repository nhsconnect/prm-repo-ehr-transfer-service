package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;

@Service
public class PdsUpdateCompletedMessageHandler extends JsQueueMessageHandler {
    public PdsUpdateCompletedMessageHandler(JmsProducer jmsProducer, @Value("${activemq.outboundQueue}") String outboundQueue){
        super(jmsProducer, outboundQueue);
    }

    @Override
    public String getInteractionId() {
        return "PRPA_IN000202UK01";
    }
}
