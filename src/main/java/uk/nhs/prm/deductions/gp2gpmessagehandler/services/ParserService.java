package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.*;

import java.io.IOException;

/*
 Can parse raw binary sanitised messages
 */
@Component
public class ParserService {
    public ParserService() {}

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
        }
        return new ParsedMessage(envelope, message, contentAsString);
    }
}
