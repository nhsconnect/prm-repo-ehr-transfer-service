package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.EhrExtractMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class S3PointerMessageHandler {


    private S3Client s3Client;

    public S3PointerMessageHandler(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ParsedMessage handle(S3PointerMessage message) throws IOException {
            var ehrMessageInputStream =
                    s3Client.getObject(GetObjectRequest.builder().bucket(message.getS3BucketName()).key(message.getS3Key()).build());
            return parse(getS3MessageContentAsString(ehrMessageInputStream));
    }
    private ParsedMessage parse(String s3Message) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        var mhsJsonMessage = new ObjectMapper().readValue(s3Message, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        var message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
        return new ParsedMessage(envelope, message, s3Message);
    }
    private String getS3MessageContentAsString(InputStream ehrMessageInputStream) {
        return new BufferedReader(
                new InputStreamReader(ehrMessageInputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
