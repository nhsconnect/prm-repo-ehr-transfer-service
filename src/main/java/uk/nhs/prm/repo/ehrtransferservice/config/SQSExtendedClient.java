package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SQSExtendedClient {

    private final S3ClientSpringConfiguration s3;

    @Value("${aws.smallEhrQueueS3}")
    private String bucketName;

    public void sqsExtendedClient () {
        var extendedClientConfiguration = new ExtendedClientConfiguration().withPayloadSupportEnabled(s3.s3Client(), bucketName,true);
    }
}
