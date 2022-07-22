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

import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LargeSqsMessageParser {
    private final S3Client s3Client;
    private final S3PointerMessageParser s3PointerMessageParser;

    public LargeSqsMessage parse(Message message) throws Exception {
        var sqsMessagePayload = ((TextMessage) message).getText();

        if (isValidS3PointerMessage(sqsMessagePayload)) {
            log.info("Going to retrieve message from s3");
            return retrieveMessageFromS3(s3PointerMessageParser.parse(sqsMessagePayload));
        }

        log.info("Not a message to be retrieved from s3, parsing directly");
        return toLargeSqsMessage(sqsMessagePayload);
    }

    public LargeSqsMessage retrieveMessageFromS3(S3PointerMessage sqsMessagePayload) throws Exception {
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(sqsMessagePayload.getS3BucketName())
                .key(sqsMessagePayload.getS3Key()).build();

        var rawMessage = new BufferedReader(
                new InputStreamReader(s3Client.getObject(getObjectRequest), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        return toLargeSqsMessage(rawMessage);
    }

    private boolean isValidS3PointerMessage(String message) {
        return message.contains(S3PointerMessage.S3_BUCKET_NAME_PROPERTY)
                && message.contains(S3PointerMessage.S3_KEY_PROPERTY);
    }

    //TODO: same stuff duplicated from Parser.parse. Room for improvement
    private LargeSqsMessage toLargeSqsMessage(String rawMessage) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        var mhsJsonMessage = new ObjectMapper().readValue(rawMessage, MhsJsonMessage.class);
        var envelope = xmlMapper.readValue(mhsJsonMessage.ebXML, SOAPEnvelope.class);
        var message = xmlMapper.readValue(mhsJsonMessage.payload, EhrExtractMessageWrapper.class);
        return new LargeSqsMessage(envelope, message, rawMessage);
    }
}