package uk.nhs.prm.deductions.gp2gpmessagehandler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
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
import software.amazon.awssdk.services.sns.SnsClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;


@TestConfiguration
public class LocalStackAwsConfig {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
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

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        recreateIncomingNemsQueue();
    }

    private void recreateIncomingNemsQueue() {
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
        }
        catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
    }
}

