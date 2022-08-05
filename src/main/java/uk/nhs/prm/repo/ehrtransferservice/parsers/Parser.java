package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.*;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

@Component
@Slf4j
public class Parser {
    private final XmlMapper xmlMapper = new XmlMapper();

    public ParsedMessage parse(String messageBodyAsString) throws IOException {
        var mhsJsonMessage = new ObjectMapper().readValue(messageBodyAsString, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        MessageContent message = null;
        switch (envelope.header.messageHeader.action) {
            case "RCMR_IN030000UK06":
                message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
            break;
            case "RCMR_IN010000UK05":
                message = xmlMapper.readValue(mhsJsonMessage.payload, EhrRequestMessageWrapper.class);
                break;
            case "MCCI_IN010000UK13":
                message = xmlMapper.readValue(mhsJsonMessage.payload, AcknowledgementMessageWrapper.class);
                return new Acknowledgement(envelope, message, messageBodyAsString);
            case "COPC_IN000001UK01":
                log.info("COPC message received in Parser");
            default:
                log.warn("No interaction ID match found for current message");
                break;
        }
        return new ParsedMessage(envelope, message, messageBodyAsString);
    }

    public String parseMessageBody(Message message) throws JMSException {
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
