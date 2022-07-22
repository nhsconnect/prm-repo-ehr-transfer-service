package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.EhrExtractMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LargeSqsMessageParser {
    private final S3Client s3Client;
    private final S3PointerMessageParser s3PointerMessageParser;

    public LargeSqsMessage getLargeSqsMessage(S3PointerMessage sqsMessagePayload) throws IOException {
        var ehrMessageInputStream =
                s3Client.getObject(GetObjectRequest.builder().bucket(sqsMessagePayload.getS3BucketName()).key(sqsMessagePayload.getS3Key()).build());
        return parse(getS3MessageContentAsString(ehrMessageInputStream));
    }

    public LargeSqsMessage getLargeSqsMessage(String sqsMessagePayload) throws IOException {
        if (isValidS3PointerMessage(sqsMessagePayload)) {
            log.info("Going to retrieve message from s3");
            return getLargeSqsMessage(s3PointerMessageParser.parse(sqsMessagePayload));
        }

        log.info("Not a message to be retrieved from s3, parsing directly");
        return parse(sqsMessagePayload);
    }

    private boolean isValidS3PointerMessage(String message) {
        return message.contains("s3BucketName") && message.contains("s3Key");
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
