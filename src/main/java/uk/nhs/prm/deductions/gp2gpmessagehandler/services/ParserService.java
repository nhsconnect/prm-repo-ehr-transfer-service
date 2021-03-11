package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;

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

    public ParserService() {

    }

    public ParsedMessage parse(String contentAsString) throws IOException, MessagingException {
        ByteArrayDataSource dataSource = new ByteArrayDataSource(contentAsString, "multipart/related;charset=\"UTF-8\"");
        MimeMultipart mimeMultipart = new MimeMultipart(dataSource);
        BodyPart soapHeader = mimeMultipart.getBodyPart(0);
        XmlMapper xmlMapper = new XmlMapper();
        InputStream inputStream = soapHeader.getInputStream();
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        SOAPEnvelope soapEnvelope = xmlMapper.readValue(content, SOAPEnvelope.class);
        return new ParsedMessage(soapEnvelope);
    }
}
