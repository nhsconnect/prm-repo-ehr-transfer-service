package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.EhrExtractMessageWrapper;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageContent;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;

import javax.jms.BytesMessage;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/*
 Can parse raw binary sanitised messages
 */
@Component
public class ParserService {
    public ParserService() {}

    public ParsedMessage parse(String contentAsString, String rawMessage) throws IOException, MessagingException {
        ByteArrayDataSource dataSource = new ByteArrayDataSource(contentAsString, "multipart/related;charset=\"UTF-8\"");
        MimeMultipart mimeMultipart = new MimeMultipart(dataSource);
        XmlMapper xmlMapper = new XmlMapper();
        SOAPEnvelope envelope = xmlMapper.readValue(getStringForIndex(mimeMultipart, 0), SOAPEnvelope.class);
        MessageContent message = null;
        if (envelope.header.messageHeader.action.equals("RCMR_IN030000UK06")) {
            message = xmlMapper.readValue(getStringForIndex(mimeMultipart, 1), EhrExtractMessageWrapper.class);
        }
        return new ParsedMessage(envelope, message, rawMessage);
    }

    private String getStringForIndex(MimeMultipart mimeMultipart, int index) throws MessagingException, IOException {
        BodyPart bodyPart = mimeMultipart.getBodyPart(index);
        InputStream inputStream = bodyPart.getInputStream();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
