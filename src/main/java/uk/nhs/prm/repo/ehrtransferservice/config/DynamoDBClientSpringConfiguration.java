package uk.nhs.prm.repo.ehrtransferservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.nhs.prm.repo.ehrtransferservice.database.model.TransferTracker;

@Configuration
@RequiredArgsConstructor
public class DynamoDBClientSpringConfiguration {
    @Value("${aws.region}")
    private String awsRegion;
    private AwsCredentialsProvider awsCredentialsProvider;
    private AppConfig appConfig;

    @Bean
    @Deprecated // To be removed in favour of using directly through the enhanced client below.
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient())
                .build();
    }

    @Bean
    public DynamoDbTable<TransferTracker> transferTrackerDynamoDbTable() {
        return dynamoDbEnhancedClient().table(
                appConfig.transferTrackerDbTableName(),
                TableSchema.fromBean(TransferTracker.class)
        );
    }
}