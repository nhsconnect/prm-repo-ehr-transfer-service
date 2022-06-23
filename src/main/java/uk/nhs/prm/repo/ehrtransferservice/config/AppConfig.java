package uk.nhs.prm.repo.ehrtransferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {

    private final String environment;
    private final String transferTrackerDbTableName;
    private final String transferCompleteQueue;
    private final String transferCompleteTopicArn;

    public AppConfig(@Value("${environment}") String environment,
                     @Value("${aws.transferTrackerDbTableName}") String transferTrackerDbTableName,
                     @Value("${aws.transferCompleteQueueName}") String transferCompleteQueue,
                     @Value("${aws.transferCompleteTopicArn}") String transferCompleteTopicArn) {
        this.environment = environment;
        this.transferTrackerDbTableName = transferTrackerDbTableName;
        this.transferCompleteQueue = transferCompleteQueue;
        this.transferCompleteTopicArn = transferCompleteTopicArn;
    }

    public String environment() {
        return environment;
    }

    public String transferTrackerDbTableName() {
        return transferTrackerDbTableName;
    }

    public String transferCompleteQueueName() {
        return transferCompleteQueue;
    }

    public String transferCompleteSnsTopicArn() {
        return transferCompleteTopicArn;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}