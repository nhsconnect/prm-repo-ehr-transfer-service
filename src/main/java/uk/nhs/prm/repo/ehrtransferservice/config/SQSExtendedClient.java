package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@RequiredArgsConstructor
public class SQSExtendedClient {
    private final SqsClient sqsClient;
    private final S3ClientSpringConfiguration s3;

    @Value("${aws.sqsLargeMessageBucketName}")
    private String bucketName;

    @Bean
    public AmazonSQSExtendedClient sqsExtendedClient () {
        var extendedClientConfiguration = new ExtendedClientConfiguration().withPayloadSupportEnabled(s3.s3Client(), bucketName, true);
        return new AmazonSQSExtendedClient(sqsClient, extendedClientConfiguration);
    }
}
