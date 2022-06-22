package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration

public class SnsClientSpringConfiguration {
    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public AmazonSNS snsClient() {
        return AmazonSNSClientBuilder.standard().withRegion(awsRegion).build();
    }

    @Bean
    public SnsClient snsClientV2() {
        return SnsClient.builder().region(Region.of(awsRegion)).build();
    }
}
