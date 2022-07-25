package uk.nhs.prm.repo.ehrtransferservice.parsers;

import com.google.gson.JsonElement;
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

    public ParsingResult<S3PointerMessage> parse(String payload) {
        var payloadAsJson = JsonParser.parseString(payload);

        if (!isValidS3PointerMessage(payloadAsJson)) {
            log.info("Message parsed is not a S3PointerMessage");
            return new ParsingResult<>(null, Status.KO);
        }
        var messageContent = payloadAsJson.getAsJsonArray().get(S3_POINTER_MESSAGE_CONTENT_INDEX).getAsJsonObject();

        var s3PointerMessage = new S3PointerMessage(messageContent);
        log.info("Successfully parsed S3PointerMessage");
        return new ParsingResult<>(s3PointerMessage, Status.OK);
    }

    private boolean isValidS3PointerMessage(JsonElement payloadAsJson) {
        if (!payloadAsJson.isJsonArray()) {
            return false;
        }

        var payloadAsJsonArray = payloadAsJson.getAsJsonArray();
        if (payloadAsJsonArray.size() != 2) {
            return false;
        }

        var firstObjInArr = payloadAsJsonArray.get(0);
        var secondObjInArr = payloadAsJsonArray.get(1);

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
}