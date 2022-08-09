package uk.nhs.prm.repo.ehrtransferservice.parsers;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

@Component
@Slf4j
public class AmqpMessageParser {
    public String parse(Message message) throws JMSException {
        log.info("Received BytesMessage from MQ");
        var bytesMessage = (BytesMessage) message;
        byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(contentAsBytes);
        var byteBuffer = ReadableBuffer.ByteBufferReader.wrap(contentAsBytes);
        var amqpMessage = org.apache.qpid.proton.message.Message.Factory.create();
        amqpMessage.decode(byteBuffer);
        return (String) ((AmqpValue) amqpMessage.getBody()).getValue();
    }
}
