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

    @Test
    void shouldProcessAndStoreInitialInformationInDb()  {
        var nhsNumber = Long.toString(System.currentTimeMillis());
        var queueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        sqs.sendMessage(queueUrl, getRepoIncomingData(nhsNumber));

        await().atMost(10,TimeUnit.SECONDS).untilAsserted(() -> {
            var scanResponse = dbClient.scan(ScanRequest.builder()
                    .tableName(transferTrackerDbTableName)
                    .build());
            var items = scanResponse.items();

            assertThat(items.get(0).get("nhs_number").n()).isEqualTo(nhsNumber);
            assertThat(items.get(0).get("nems_message_id").s()).isEqualTo("NEMS-MESSAGE-ID");
            conversationId = items.get(0).get("conversation_id").s();
            assertThat(conversationId.length()).isEqualTo(UUID.randomUUID().toString().length());
        });
    }

    private String getRepoIncomingData(String nhsNumber) {
        return "{ \"nhsNumber\" : \"" + nhsNumber + "\",\n \"nemsMessageId\" :\"NEMS-MESSAGE-ID\", \"sourceGp\": \"S0URC3\", \"destinationGp\": \"D3ST1NAT10N\" }";
    }
}
