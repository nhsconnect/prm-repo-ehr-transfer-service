package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Component;
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

    private AmazonS3 s3Client;
    private final Parser parser;

    public S3PointerMessageHandler(AmazonS3 s3Client, Parser parser) {
        this.s3Client = s3Client;
        this.parser = parser;
    }

    public ParsedMessage handle(S3PointerMessage message) {
        try {
            var ehrMessageInputStream = s3Client.getObject(message.getS3BucketName(), message.getS3Key()).getObjectContent();
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
