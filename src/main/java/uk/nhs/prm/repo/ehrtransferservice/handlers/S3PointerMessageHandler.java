package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.json_models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class S3PointerMessageHandler {


    private S3Client s3Client;
    private final Parser parser;

    public S3PointerMessageHandler(S3Client s3Client, Parser parser) {
        this.s3Client = s3Client;
        this.parser = parser;
    }

    public ParsedMessage handle(S3PointerMessage message) {
        try {
            var ehrMessageInputStream =
                    s3Client.getObject(GetObjectRequest.builder().bucket(message.getS3BucketName()).key(message.getS3Key()).build());
            return parser.parse(getS3MessageContentAsString(ehrMessageInputStream));
        } catch (Exception e) {
            throw new RuntimeException("Encountered exception while parsing s3 message", e);//TODO:Add more context
        }
    }

    private String getS3MessageContentAsString(InputStream ehrMessageInputStream) {
        return new BufferedReader(
                new InputStreamReader(ehrMessageInputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
