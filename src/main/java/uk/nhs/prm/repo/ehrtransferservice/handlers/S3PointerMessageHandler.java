package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.EhrExtractMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.S3PointerMessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class S3PointerMessageHandler {


    private S3Client s3Client;
    private S3PointerMessageParser s3PointerMessageParser;

    public S3PointerMessageHandler(S3Client s3Client, S3PointerMessageParser s3PointerMessageParser) {
        this.s3Client = s3Client;
        this.s3PointerMessageParser = s3PointerMessageParser;
    }

    public LargeSqsMessage getLargeSqsMessage(S3PointerMessage sqsMessagePayload) throws IOException {
        var ehrMessageInputStream =
                s3Client.getObject(GetObjectRequest.builder().bucket(sqsMessagePayload.getS3BucketName()).key(sqsMessagePayload.getS3Key()).build());
        return parse(getS3MessageContentAsString(ehrMessageInputStream));
    }

    public LargeSqsMessage getLargeSqsMessage(String sqsMessagePayload) throws IOException {
        return getLargeSqsMessage(s3PointerMessageParser.parse(sqsMessagePayload));
    }

    private LargeSqsMessage parse(String s3Message) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        var mhsJsonMessage = new ObjectMapper().readValue(s3Message, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        var message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
        return new LargeSqsMessage(envelope, message, s3Message);
    }

    private String getS3MessageContentAsString(InputStream ehrMessageInputStream) {
        return new BufferedReader(
                new InputStreamReader(ehrMessageInputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
