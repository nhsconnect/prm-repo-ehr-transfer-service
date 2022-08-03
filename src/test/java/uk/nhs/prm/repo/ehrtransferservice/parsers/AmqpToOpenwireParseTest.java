package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.CompositeWritableBuffer;
import org.apache.qpid.proton.codec.DroppingWritableBuffer;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.ProtonJMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class AmqpToOpenwireParseTest {

    private final Parser parser = new Parser();
    String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<RCMR_IN030000UK06 xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:hl7-org:v3 ..SchemasRCMR_IN030000UK06.xsd\">\n" +
            "    <ControlActEvent classCode=\"CACT\" moodCode=\"EVN\">\n" +
            "        <subject typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
            "            <EhrExtract classCode=\"EXTRACT\" moodCode=\"EVN\">\n" +
            "                <recordTarget typeCode=\"RCT\">\n" +
            "                    <patient classCode=\"PAT\">\n" +
            "                        <id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9442964410\" />\n" +
            "                    </patient>" +
            "                </recordTarget>" +
            "             </EhrExtract>" +
            "         </subject>" +
            "    </ControlActEvent>" +
            "</RCMR_IN030000UK06>";

    private ByteBuffer magicCodeCopiedFromActiveMQCodebaseItself(org.apache.qpid.proton.message.Message message) {
        ProtonJMessage amqp = (ProtonJMessage) message;

        ByteBuffer buffer = ByteBuffer.wrap(new byte[1024 * 4]);
        final DroppingWritableBuffer overflow = new DroppingWritableBuffer();
        int c = amqp.encode(new CompositeWritableBuffer(new WritableBuffer.ByteBufferWrapper(buffer), overflow));
        if (overflow.position() > 0) {
            buffer = ByteBuffer.wrap(new byte[1024 * 4 + overflow.position()]);
            c = amqp.encode(new WritableBuffer.ByteBufferWrapper(buffer));
        }

        return buffer;
//        return new EncodedMessage(1, buffer.array(), 0, c);
    }

    private Message createAmqpMessage(){
        var json = new JsonObject();
        json.addProperty("ebXML", extract);
        // THIS GET PARSED, then it fails in the creation of ParseMessage
        // to verify if there are all needed fields in it
        var originalString = new Gson().toJson(json);

        System.out.println("original message is");
        System.out.println(originalString);

        var body = new AmqpValue(originalString);
        var message = Message.Factory.create();
        message.setBody(body);
        message.setContentType("application/json");
        message.setMessageId(UUID.randomUUID());
        return message;
    }

    private byte[] amqpToBytes(Message message) {
        var buffer = magicCodeCopiedFromActiveMQCodebaseItself(message);
        var writableBuffer = new WritableBuffer.ByteBufferWrapper(buffer);
        message.encode(writableBuffer);
        var bytes = writableBuffer.toReadableBuffer().array();
        return bytes;
    }

    private ActiveMQBytesMessage bytesToOpenWireMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(bytes);
        bytesMessage.reset();
        return bytesMessage;
    }

    // This test is an utility to iterate faster trying to construct a message using proton libraries
    // and ensuring we're able to parse it back when it enters our application.
    // so the flow is:
    // org.apache.qpid.proton.message.Message -> bytes
    // then
    // bytes -> javax.jms.Message
    // then
    // javax.jms.Message -> ParsedMessage
    @Disabled("WIP")
    @Test
    void aMessageShouldBeCreatedAsAmqpValueAndBeingParsedAsOpenWire() throws JMSException, IOException {
        var amqpMessage = createAmqpMessage();
        var bytes = amqpToBytes(amqpMessage);
        var openWireMessage = bytesToOpenWireMessage(bytes);
        var messageBody = parser.parseMessageBody(openWireMessage);

        var parsedMessage = parser.parse(messageBody);

        assertThat(parsedMessage).isNotNull();
    }
}
