package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.payloadoffloading.PayloadStorageConfiguration;
import software.amazon.sns.AmazonSNSExtendedClient;
import software.amazon.sns.SNSExtendedClientConfiguration;

@Configuration
@RequiredArgsConstructor
public class SNSExtendedClient {

    private final S3ClientSpringConfiguration s3;

    @Value("${aws.sqsLargeMessageBucketName}")
    private String bucketName;

    @Value("${aws.smallEhrTopicArn}")
    private String topicArn;

    @Bean
    public AmazonSNSExtendedClient snsExtendedClient () {
        PayloadStorageConfiguration payloadStorageConfiguration = new SNSExtendedClientConfiguration().withPayloadSupportEnabled(s3.s3Client(), bucketName).withPayloadSizeThreshold(32);
        return new AmazonSNSExtendedClient(getSNSClient(), (SNSExtendedClientConfiguration) payloadStorageConfiguration);
//        snsExtendedClient.publish(topicArn, message);
    }
    //TODO: changed the region hardcoded to parameterised env value
    public AmazonSNS getSNSClient(){
     return AmazonSNSClientBuilder.standard().withRegion("eu-west-2").build();
    }
}
