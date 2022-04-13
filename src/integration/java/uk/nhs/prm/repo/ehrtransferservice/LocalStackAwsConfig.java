package uk.nhs.prm.repo.ehrtransferservice;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.sns.AmazonSNSExtendedClient;
import software.amazon.sns.SNSExtendedClientConfiguration;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@TestConfiguration
public class LocalStackAwsConfig {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;
    @Autowired
    private DynamoDbClient dynamoDbClient;
    @Autowired
    private S3Client s3Client;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    @Value("${aws.sqsLargeMessageBucketName}")
    private String sqsLargeMessageBucketName;

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static AmazonSNSExtendedClient snsClient(@Value("${localstack.url}") String localstackUrl) {
        var amazonSNS = AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
        return new AmazonSNSExtendedClient(amazonSNS, new SNSExtendedClientConfiguration());
    }

    @Bean
    public static S3Client s3Client(@Value("${localstack.url}") String localstackUrl) {
        return S3Client.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return "FAKE";
                    }

                    @Override
                    public String secretAccessKey() {
                        return "FAKE";
                    }
                }))
                .build();
    }

    @Bean
    public static DynamoDbClient dynamoDbClient(@Value("${localstack.url}") String localstackUrl) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(localstackUrl))
                .region(Region.EU_WEST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(new AwsCredentials() {
                            @Override
                            public String accessKeyId() {
                                return "FAKE";
                            }

                            @Override
                            public String secretAccessKey() {
                                return "FAKE";
                            }
                        }))
                .build();
    }

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        recreateIncomingQueue();
        setupDbAndTable();
        setupS3Bucket();
    }

    private void setupS3Bucket() {
        var waiter = s3Client.waiter();
        var createBucketRequest = CreateBucketRequest.builder()
                .bucket(sqsLargeMessageBucketName)
                .grantFullControl("GrantFullControl")
                .build();
        if (s3Client.listBuckets().hasBuckets()) {
            resetS3ForLocalEnvironment(waiter);
        }

        s3Client.createBucket(createBucketRequest);
        waiter.waitUntilBucketExists(HeadBucketRequest.builder().bucket(sqsLargeMessageBucketName).build());
    }

    private void setupDbAndTable() {
        var waiter = dynamoDbClient.waiter();
        var tableRequest = DescribeTableRequest.builder()
                .tableName(transferTrackerDbTableName)
                .build();

        if (dynamoDbClient.listTables().tableNames().contains(transferTrackerDbTableName)) {
            resetTableForLocalEnvironment(waiter, tableRequest);
        }

        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(KeySchemaElement.builder()
                .keyType(KeyType.HASH)
                .attributeName("conversation_id")
                .build());
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(AttributeDefinition.builder()
                .attributeType(ScalarAttributeType.S)
                .attributeName("conversation_id")
                .build());

        var createTableRequest = CreateTableRequest.builder()
                .tableName(transferTrackerDbTableName)
                .keySchema(keySchema)
                .attributeDefinitions(attributeDefinitions)
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);
        waiter.waitUntilTableExists(tableRequest);
    }

    private void recreateIncomingQueue() {
        ensureQueueDeleted(repoIncomingQueueName);
        createQueue(repoIncomingQueueName);
    }

    private void createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setQueueName(queueName);
        HashMap<String, String> attributes = new HashMap<>();
        createQueueRequest.withAttributes(attributes);
        amazonSQSAsync.createQueue(queueName);
    }

    private void ensureQueueDeleted(String queueName) {
        try {
            deleteQueue(queueName);
        } catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
    }

    private void resetTableForLocalEnvironment(DynamoDbWaiter waiter, DescribeTableRequest tableRequest) {
        var deleteRequest = DeleteTableRequest.builder().tableName(transferTrackerDbTableName).build();
        dynamoDbClient.deleteTable(deleteRequest);
        waiter.waitUntilTableNotExists(tableRequest);
    }

    private void resetS3ForLocalEnvironment(S3Waiter waiter) {
        s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(sqsLargeMessageBucketName).build());
        waiter.waitUntilBucketNotExists(HeadBucketRequest.builder().bucket(sqsLargeMessageBucketName).build());
    }
}

