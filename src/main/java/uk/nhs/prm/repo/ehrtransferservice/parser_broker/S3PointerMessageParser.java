package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;

@Slf4j
@Component
public class S3PointerMessageParser {
    private static final Integer S3_POINTER_MESSAGE_CONTENT_INDEX = 1;

    public S3PointerMessage parse(String payLoad) {
        try {
            var messagePayload = JsonParser.parseString(payLoad).getAsJsonArray();
            var s3PointerMessage =  new S3PointerMessage(messagePayload.get(S3_POINTER_MESSAGE_CONTENT_INDEX).getAsJsonObject());
            log.info("Successfully parsed S3PointerMessage");
            return s3PointerMessage;
        } catch (Exception e) {
            log.error("Encountered error while parsing S3PointerMessage", e);
            throw new IllegalArgumentException("Encountered error while parsing S3PointerMessage",e);
        }
    }
}
