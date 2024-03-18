package uk.nhs.prm.repo.ehrtransferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    private final String environment;
    private final String transferTrackerDbTableName;

    @Deprecated
    private final String transferCompleteTopicArn;

    public AppConfig(@Value("${environment}") String environment,
                     @Value("${aws.transferTrackerDbTableName}") String transferTrackerDbTableName,
                     @Value("${aws.transferCompleteTopicArn}") String transferCompleteTopicArn) {
        this.environment = environment;
        this.transferTrackerDbTableName = transferTrackerDbTableName;
        this.transferCompleteTopicArn = transferCompleteTopicArn;
    }

    public String environment() {
        return environment;
    }

    public String transferTrackerDbTableName() {
        return transferTrackerDbTableName;
    }

    @Deprecated
    public String transferCompleteSnsTopicArn() {
        return transferCompleteTopicArn;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}