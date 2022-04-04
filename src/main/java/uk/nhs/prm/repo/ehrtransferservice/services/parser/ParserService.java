package uk.nhs.prm.repo.ehrtransferservice.services.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.*;

import java.io.IOException;

/*
 Can parse raw binary sanitised messages
 */
@Component
public class ParserService {
    public ParsedMessage parse(String contentAsString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MhsJsonMessage mhsJsonMessage = objectMapper.readValue(contentAsString, MhsJsonMessage.class);
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        MessageContent message = null;
        if (envelope.header.messageHeader.action.equals("RCMR_IN030000UK06")) {
            message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
        } else if (envelope.header.messageHeader.action.equals("RCMR_IN010000UK05")) {
            message = xmlMapper.readValue(mhsJsonMessage.payload, EhrRequestMessageWrapper.class);
        } else if (envelope.header.messageHeader.action.equals("MCCI_IN010000UK13")) {
            message = xmlMapper.readValue(mhsJsonMessage.payload, AcknowledgementMessageWrapper.class);
        }
        return new ParsedMessage(envelope, message, contentAsString);
    }
}
