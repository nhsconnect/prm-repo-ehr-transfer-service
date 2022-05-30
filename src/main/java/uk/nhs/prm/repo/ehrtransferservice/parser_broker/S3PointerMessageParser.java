package uk.nhs.prm.repo.ehrtransferservice.parser_broker;


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.prm.repo.ehrtransferservice.json_models.S3PointerMessage;

@Slf4j
public class S3PointerMessageParser {
    private static final Integer S3_POINTER_MESSAGE_CONTENT_INDEX = 1;

    public S3PointerMessage parse(String payLoad) {
        try {
            JsonArray messagePayload = JsonParser.parseString(payLoad).getAsJsonArray();
            S3PointerMessage s3PointerMessage =  new S3PointerMessage(messagePayload.get(S3_POINTER_MESSAGE_CONTENT_INDEX).getAsJsonObject());
            log.info("Successfully parsed S3PointerMessage");
            return s3PointerMessage;
        } catch (Exception e) {
            log.error("Encountered error while parsing S3PointerMessage");
            throw new IllegalArgumentException("Encountered error while parsing S3PointerMessage",e);
        }
    }



}