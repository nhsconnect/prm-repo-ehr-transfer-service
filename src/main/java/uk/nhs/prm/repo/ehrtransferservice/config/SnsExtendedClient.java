package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazonaws.services.sns.AmazonSNS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.payloadoffloading.PayloadStorageConfiguration;
import software.amazon.payloadoffloading.S3BackedPayloadStore;
import software.amazon.payloadoffloading.S3Dao;
import software.amazon.sns.AmazonSNSExtendedClient;
import software.amazon.sns.SNSExtendedClientConfiguration;

@Configuration
public class SnsExtendedClient {
    @Value("${aws.sqsLargeMessageBucketName}")
    private String bucketName;

    @Bean
    public AmazonSNSExtendedClient s3SupportedSnsClient(AmazonSNS snsClient, S3Client s3) {
        var s3BackedPayloadStore = new S3BackedPayloadStore(new S3Dao(s3), bucketName);
        PayloadStorageConfiguration payloadStorageConfiguration = new SNSExtendedClientConfiguration().withPayloadSupportEnabled(s3, bucketName);
        return new AmazonSNSExtendedClient(snsClient, (SNSExtendedClientConfiguration) payloadStorageConfiguration, s3BackedPayloadStore);
    }
}
