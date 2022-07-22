package uk.nhs.prm.repo.ehrtransferservice.models;

import com.google.gson.JsonObject;

public class S3PointerMessage {
    final private String s3BucketName;
    final private String s3Key;

    public S3PointerMessage(JsonObject s3PointerJsonObject) {
        this.s3BucketName = s3PointerJsonObject.get("s3BucketName").getAsString();
        this.s3Key = s3PointerJsonObject.get("s3Key").getAsString();
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3Key() {
        return s3Key;
    }
}
