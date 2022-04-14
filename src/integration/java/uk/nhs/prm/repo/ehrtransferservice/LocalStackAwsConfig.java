package uk.nhs.prm.repo.ehrtransferservice;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
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
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.payloadoffloading.S3BackedPayloadStore;
import software.amazon.payloadoffloading.S3Dao;
import software.amazon.sns.AmazonSNSExtendedClient;
import software.amazon.sns.SNSExtendedClientConfiguration;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static SqsClient sqsClient(@Value("${localstack.url}") String localstackUrl) {
        return SqsClient.builder()
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
                })).build();
    }

    @Bean
    public static AmazonSNS amazonSNS(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static AmazonSNSExtendedClient snsExtendedClient(AmazonSNS amazonSNS, S3Client s3) {
        return new AmazonSNSExtendedClient(amazonSNS, new SNSExtendedClientConfiguration(), new S3BackedPayloadStore(new S3Dao(s3), "test-s3-bucket-name-cant-have-underscores"));
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
        setupS3Bucket();
        setUpQueueAndTopics();
        setupDbAndTable();
    }

    private void setUpQueueAndTopics() {
        ensureQueueDeleted(repoIncomingQueueName);
        amazonSQSAsync.createQueue(repoIncomingQueueName);

//        var smallEhrQueue = sqsExtendedClient.createQueue(CreateQueueRequest.builder().queueName(smallEhrQueueName).build());
//        var topic = snsExtendedClient.createTopic("test_small_ehr_topic");
//        createSnsTestReceiverSubscription(topic, getQueueArn(smallEhrQueue.queueUrl()));
    }

    private void ensureQueueDeleted(String queueName) {
        try {
            amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
        } catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void setupS3Bucket() {
        var waiter = s3Client.waiter();
        var createBucketRequest = CreateBucketRequest.builder()
                .bucket(sqsLargeMessageBucketName)
                .grantFullControl("GrantFullControl")
                .build();
        for (Bucket bucket: s3Client.listBuckets().buckets()) {
            if (Objects.equals(bucket.name(), sqsLargeMessageBucketName)) {
                resetS3ForLocalEnvironment(waiter);
                break;
            }
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

    private void resetTableForLocalEnvironment(DynamoDbWaiter waiter, DescribeTableRequest tableRequest) {
        var deleteRequest = DeleteTableRequest.builder().tableName(transferTrackerDbTableName).build();
        dynamoDbClient.deleteTable(deleteRequest);
        waiter.waitUntilTableNotExists(tableRequest);
    }

    private void resetS3ForLocalEnvironment(S3Waiter waiter) {
        s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(sqsLargeMessageBucketName).build());
        waiter.waitUntilBucketNotExists(HeadBucketRequest.builder().bucket(sqsLargeMessageBucketName).build());
    }

//    private void createSnsTestReceiverSubscription(CreateTopicResult topic, String queueArn) {
//        final Map<String, String> attributes = new HashMap<>();
//        attributes.put("RawMessageDelivery", "True");
//        var subscribeRequest = new SubscribeRequest()
//                .withTopicArn(topic.getTopicArn())
//                .withProtocol("sqs")
//                .withEndpoint(queueArn)
//                .withAttributes(attributes);
//
//        snsExtendedClient.subscribe(subscribeRequest);
//    }
//
//    private String getQueueArn(String queueUrl) {
//        GetQueueAttributesResult queueAttributes = amazonSQSAsync.getQueueAttributes(queueUrl, List.of("QueueArn"));
//        return queueAttributes.getAttributes().get("QueueArn");
//    }
}

