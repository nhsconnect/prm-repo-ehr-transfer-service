package uk.nhs.prm.repo.ehrtransferservice;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;

import java.io.IOException;

public class SimpleAmqpQueue {
    private final String queueName;

    public SimpleAmqpQueue(String queueName) {
        this.queueName = queueName;
    }

    public void sendMessage(String messageBody) {
        AMQPContext ctx = new AMQPContext(AMQPContext.CLIENT);
        Connection connection = new Connection(ctx, "localhost", 5672, true);
        try {
            connection.connect();
            Session session = connection.createSession(100, 100);
            Producer p = session.createProducer(this.queueName, QoS.AT_LEAST_ONCE);
            AMQPMessage msg = new AMQPMessage();
            msg.setAmqpValue(new AmqpValue(new AMQPString(messageBody)));
            p.send(msg);
            p.close();
        } catch (AMQPException e) {
            throw new RuntimeException(e);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedProtocolVersionException e) {
            throw new RuntimeException(e);
        }
    }
}
