package uk.nhs.prm.repo.ehrtransferservice.models;


import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class S3PointerMessage {
    String s3BucketName;
    String s3Key;

    public S3PointerMessage(JsonObject s3PointerJsonObject) {
        this.s3BucketName = s3PointerJsonObject.get("s3BucketName").getAsString();
        this.s3Key = s3PointerJsonObject.get("s3Key").getAsString();
    }
}
