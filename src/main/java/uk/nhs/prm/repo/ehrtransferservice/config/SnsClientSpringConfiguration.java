package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class SnsClientSpringConfiguration {
    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public AmazonSNS snsClient() {
        return AmazonSNSClientBuilder.standard().withRegion(awsRegion).build();
    }
}
