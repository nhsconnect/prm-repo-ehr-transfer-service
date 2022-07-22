package uk.nhs.prm.repo.ehrtransferservice.models;

import com.google.gson.JsonObject;

public class S3PointerMessage {
    final public static String S3_BUCKET_NAME_PROPERTY = "s3BucketName";
    final public static String S3_KEY_PROPERTY = "s3Key";
    final private String s3BucketName;
    final private String s3Key;

    public S3PointerMessage(JsonObject s3PointerJsonObject) {
        this.s3BucketName = s3PointerJsonObject.get(S3_BUCKET_NAME_PROPERTY).getAsString();
        this.s3Key = s3PointerJsonObject.get(S3_KEY_PROPERTY).getAsString();
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3Key() {
        return s3Key;
    }
}
