package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.ParsingResult;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.Status;

@Slf4j
@Component
public class S3PointerMessageParser {
    private final Integer S3_POINTER_MESSAGE_CONTENT_INDEX = 1;
    private final String s3PointerHeader = "software.amazon.payloadoffloading.PayloadS3Pointer";

    private boolean isValidS3PointerMessage(JsonArray message) {
        if (message.size() != 2) {
            return false;
        }

        var firstObjInArr = message.get(0);
        var secondObjInArr = message.get(1);

        if (!s3PointerHeader.equals(firstObjInArr.getAsString())) {
            return false;
        }

        if (secondObjInArr.getAsJsonObject().get(S3PointerMessage.S3_BUCKET_NAME_PROPERTY) == null) {
            return false;
        }

        if (secondObjInArr.getAsJsonObject().get(S3PointerMessage.S3_KEY_PROPERTY) == null) {
            return false;
        }

        return true;
    }

    public ParsingResult<S3PointerMessage> parse(String payLoad) {
        var messagePayload = JsonParser.parseString(payLoad).getAsJsonArray();

        if (!isValidS3PointerMessage(messagePayload)) {
            log.info("Message parsed is not a S3PointerMessage");
            return new ParsingResult<>(null, Status.KO);
        }

        var s3PointerMessage = new S3PointerMessage(messagePayload.get(S3_POINTER_MESSAGE_CONTENT_INDEX).getAsJsonObject());
        log.info("Successfully parsed S3PointerMessage");
        return new ParsingResult<>(s3PointerMessage, Status.OK);
    }
}