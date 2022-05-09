package uk.nhs.prm.repo.ehrtransferservice;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.payloadoffloading.S3BackedPayloadStore;
import software.amazon.payloadoffloading.S3Dao;
import software.amazon.sns.AmazonSNSExtendedClient;
import software.amazon.sns.SNSExtendedClientConfiguration;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import java.net.URI;
import java.util.*;

import static javax.jms.Session.CLIENT_ACKNOWLEDGE;

@TestConfiguration
public class LocalStackAwsConfig {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;
    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    @Value("${aws.sqsLargeMessageBucketName}")
    private String sqsLargeMessageBucketName;

    @Value("${aws.attachmentsQueueName}")
    private String attachmentsQueueName;

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

    @Value("${aws.positiveAcksQueueName}")
    private String positiveAcksQueueName;

    @Value("${aws.parsingDlqQueueName}")
    private String parsingDlqQueueName;

    @Value("${activemq.amqEndpoint1}")
    private String amqEndpoint1;

    @Value("${activemq.amqEndpoint2}")
    private String amqEndpoint2;

    @Value("${activemq.userName}")
    private String brokerUsername;

    @Value("${activemq.password}")
    private String brokerPassword;

    @Value("${activemq.randomOption}")
    private String randomOption;

    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(CLIENT_ACKNOWLEDGE);
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(failoverUrl());
        activeMQConnectionFactory.setPassword(brokerPassword);
        activeMQConnectionFactory.setUserName(brokerUsername);
        return activeMQConnectionFactory;
    }

    private String failoverUrl() {
        return String.format("failover:(%s,%s)%s", amqEndpoint1, amqEndpoint2, randomOption);
    }

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
    public static AmazonSQSExtendedClient s3SupportedSqsClient(AmazonSQSAsync sqsClient, AmazonS3 s3) {
        return new AmazonSQSExtendedClient(sqsClient, new ExtendedClientConfiguration().withPayloadSupportEnabled(s3, "test-s3-bucket-name-cant-have-underscores", true));
    }

    @Bean
    public static AmazonSNS amazonSNS(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static AmazonSNSExtendedClient s3SupportedSnsClient(AmazonSNS amazonSNS, AmazonS3 s3) {
        return new AmazonSNSExtendedClient(amazonSNS, new SNSExtendedClientConfiguration(), new S3BackedPayloadStore(new S3Dao(s3), "test-s3-bucket-name-cant-have-underscores"));
    }

    @Bean
    public static SnsClient snsClient(@Value("${localstack.url}") String localstackUrl) {
        return SnsClient.builder()
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
    public AmazonS3 amazonS3Client(@Value("${localstack.url}") String localstackUrl) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
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
        amazonSQSAsync.createQueue(repoIncomingQueueName);

        var attachmentQueue = amazonSQSAsync.createQueue(attachmentsQueueName);
        var attachmentsTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_attachments_topic").build());
        createSnsTestReceiverSubscription(attachmentsTopic, getQueueArn(attachmentQueue.getQueueUrl()));

        var smallEhrQueue = amazonSQSAsync.createQueue(smallEhrQueueName);
        var smallEhrTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_small_ehr_topic").build());
        createSnsTestReceiverSubscription(smallEhrTopic, getQueueArn(smallEhrQueue.getQueueUrl()));

        var positiveAcksTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_positive_acks_topic").build());
        var positiveAcksQueue = amazonSQSAsync.createQueue(positiveAcksQueueName);
        createSnsTestReceiverSubscription(positiveAcksTopic, getQueueArn(positiveAcksQueue.getQueueUrl()));

        var parsingDlqTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_dlq_topic").build());
        var parsingDlqQueue = amazonSQSAsync.createQueue(parsingDlqQueueName);
        createSnsTestReceiverSubscription(parsingDlqTopic, getQueueArn(parsingDlqQueue.getQueueUrl()));
    }

    private void setupS3Bucket() {
        var waiter = s3Client.waiter();
        var createBucketRequest = CreateBucketRequest.builder()
                .bucket(sqsLargeMessageBucketName)
                .grantFullControl("GrantFullControl")
                .build();
        for (var bucket: s3Client.listBuckets().buckets()) {
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

    private void createSnsTestReceiverSubscription(CreateTopicResponse topic, String queueArn) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("RawMessageDelivery", "True");
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topic.topicArn())
                .protocol("sqs")
                .endpoint(queueArn)
                .attributes(attributes)
                .build();

        snsClient.subscribe(subscribeRequest);
    }

    private String getQueueArn(String queueUrl) {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(queueUrl, List.of("QueueArn"));
        return queueAttributes.getAttributes().get("QueueArn");
    }
}

