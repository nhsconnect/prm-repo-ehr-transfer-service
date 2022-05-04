package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.*;

import java.io.IOException;

/*
 Can parse raw binary sanitised messages
 */
@Component
@Slf4j
public class Parser {
    private final XmlMapper xmlMapper = new XmlMapper();

    public ParsedMessage parse(String contentAsString) throws IOException {
        var mhsJsonMessage = new ObjectMapper().readValue(contentAsString, MhsJsonMessage.class);
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
                break;
            //TODO: AttachmentMessageWrapper for COPC_IN000001UK01 needs so be implemented. Happy path works now,
            //but a specific wrapper will be necessary whey playing NACK stories
            case "COPC_IN000001UK01":
            default:
                log.warn("No interaction ID match found for current message");
                break;
        }
        return new ParsedMessage(envelope, message, contentAsString);
    }
}
