package uk.nhs.prm.repo.ehrtransferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final String environment;
    private final String transferTrackerDbTableName;
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

    public String transferCompleteTopicArn() {
        return transferCompleteTopicArn;
    }
}