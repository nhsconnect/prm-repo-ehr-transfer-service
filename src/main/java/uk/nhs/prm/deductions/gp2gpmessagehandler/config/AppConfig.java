package uk.nhs.prm.deductions.gp2gpmessagehandler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final String environment;
    private final String transferTrackerDbTableName;

    public AppConfig(@Value("${environment}") String environment,
                     @Value("${aws.transferTrackerDbTableName}") String transferTrackerDbTableName) {
        this.environment = environment;
        this.transferTrackerDbTableName = transferTrackerDbTableName;
    }

    public String environment() {
        return environment;
    }

    public String suspensionDynamoDbTableName() { return transferTrackerDbTableName; }
}
