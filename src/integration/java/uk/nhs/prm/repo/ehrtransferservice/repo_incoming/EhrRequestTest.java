package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class EhrRequestTest {
    @Autowired
    private AmazonSQSAsync sqs;

    @Autowired
    private DynamoDbClient dbClient;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    private String conversationId;
    private static final String NHS_NUMBER = "2222222222";
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String SOURCE_GP = "odscode";

    @AfterEach
    void tearDown() {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());
        dbClient.deleteItem(DeleteItemRequest.builder().tableName(transferTrackerDbTableName).key(key).build());
    }

    @Test
    void shouldProcessAndStoreInitialInformationInDb()  {
        var queueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        sqs.sendMessage(queueUrl, getRepoIncomingData());

        await().atMost(10,TimeUnit.SECONDS).untilAsserted(() -> {
            var scanResponse = dbClient.scan(ScanRequest.builder()
                    .tableName(transferTrackerDbTableName)
                    .build());
            var items = scanResponse.items();

            assertThat(items.get(0).get("nhs_number").n()).isEqualTo(NHS_NUMBER);
            assertThat(items.get(0).get("nems_message_id").s()).isEqualTo(NEMS_MESSAGE_ID);
            assertThat(items.get(0).get("source_gp").s()).isEqualTo(SOURCE_GP);
            conversationId = items.get(0).get("conversation_id").s();
            assertThat(conversationId.length()).isEqualTo(UUID.randomUUID().toString().length());
        });
    }

    private String getRepoIncomingData() {
        return "{ \"nhsNumber\" : \"" + NHS_NUMBER + "\",\n \"nemsMessageId\" : \"" + NEMS_MESSAGE_ID + "\",\n \"sourceGp\": \"" + SOURCE_GP + "\",\n \"destinationGp\": \"D3ST1NAT10N\" }";
    }
}
