package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
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

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;

    private static final String NHS_NUMBER = "2222222222";
    private static final String CONVERSATION_ID = "000-111-222-333-444";
    private static final String NEMS_MESSAGE_ID = "eefe01f7-33aa-45ed-8aac-4e0cf68670fd";
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "odscode";
    private WireMockServer wireMock;

    @BeforeEach
    public void setUp() {
        wireMock = initializeWebServer();
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @AfterEach
    void tearDown() {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(CONVERSATION_ID).build());
        dbClient.deleteItem(DeleteItemRequest.builder().tableName(transferTrackerDbTableName).key(key).build());
        var queueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));

        wireMock.resetAll();
        wireMock.stop();
    }

    @Test
    void shouldSendEhrRequestAndUpdateDbWhenMessageOnRepoIncomingQueue()  {
        var queueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        stubFor(post(urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));

        sqs.sendMessage(queueUrl, getRepoIncomingData());

        await().atMost(20,TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("conversation_id", AttributeValue.builder().s(CONVERSATION_ID).build());

            var dbClientItem = dbClient.getItem(GetItemRequest.builder()
                    .tableName(transferTrackerDbTableName)
                    .key(key)
                    .build()).item();

            assertThat(dbClientItem.get("nhs_number").s()).isEqualTo(NHS_NUMBER);
            assertThat(dbClientItem.get("nems_message_id").s()).isEqualTo(NEMS_MESSAGE_ID);
            assertThat(dbClientItem.get("source_gp").s()).isEqualTo(SOURCE_GP);
            assertThat(dbClientItem.get("nems_event_last_updated").s()).isEqualTo(NEMS_EVENT_LAST_UPDATED);
            assertThat(dbClientItem.get("conversation_id").s()).isEqualTo(CONVERSATION_ID);
            assertThat(dbClientItem.get("state").s()).isEqualTo("ACTION:EHR_REQUEST_SENT");
        });
    }

    private String getRepoIncomingData() {
        return "{ \"conversationId\" : \"" + CONVERSATION_ID + "\",\n \"nhsNumber\" : \"" + NHS_NUMBER + "\",\n \"nemsMessageId\" : \"" + NEMS_MESSAGE_ID + "\",\n \"sourceGp\": \"" + SOURCE_GP + "\",\n \"destinationGp\": \"D3ST1NAT10N\" ,\n \"nemsEventLastUpdated\": \"" + NEMS_EVENT_LAST_UPDATED + "\"}";
    }
}
