package uk.nhs.prm.repo.ehrtransferservice.handler;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.awaitility.Duration;
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
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
public class EhrRequestTimeoutHandlerTest {

    @Autowired
    private AmazonSQSAsync sqs;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;
    @Value("${aws.transferCompleteQueueName}")
    private String transferCompleteQueueName;
    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    @Autowired
    private DynamoDbClient dbClient;

    @Value("${gp2gpMessengerAuthKey}")
    private String gp2gpMessengerAuthKey;


    private static final String NHS_NUMBER = "2222222222";
    private static final String CONVERSATION_ID = UUID.randomUUID().toString();
    private static final String NEMS_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String NEMS_EVENT_LAST_UPDATED = "2017-11-01T15:00:33+00:00";
    private static final String SOURCE_GP = "odscode";

    private WireMockServer wireMock;

    @BeforeEach
    public void setUp() {
        wireMock = initializeWebServer();
        var queueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
        var transferCompleteQueueUrl = sqs.getQueueUrl(transferCompleteQueueName).getQueueUrl();
        sqs.purgeQueue(new PurgeQueueRequest(transferCompleteQueueUrl));
    }

    @AfterEach
    void tearDown() {
        wireMock.resetAll();
        wireMock.stop();
    }

    private WireMockServer initializeWebServer() {
        final WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        return wireMockServer;
    }

    @Test
    void shouldInvokeHandleMethodInEhrRequestTimeoutHandlerAtScheduledTimePeriod() {

        //put message on repo incoming
        var repoIncomingQueueUrl = sqs.getQueueUrl(repoIncomingQueueName).getQueueUrl();
        sqs.sendMessage(repoIncomingQueueUrl, getRepoIncomingData());

        var transferCompleteQUrl = sqs.getQueueUrl(transferCompleteQueueName).getQueueUrl();


        //stub call to gp2gp messenger to return 200
        stubFor(post(urlMatching("/health-record-requests/" + NHS_NUMBER))
                .withHeader("Authorization", equalTo(gp2gpMessengerAuthKey))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(204)));
        //assert that transfer complete queue received message

        await()
                .atMost(Duration.ONE_MINUTE)
                .untilAsserted(() -> {
                    Map<String, AttributeValue> key = new HashMap<>();
                    key.put("conversation_id", AttributeValue.builder().s(CONVERSATION_ID).build());
                    var dbClientItem = dbClient.getItem(GetItemRequest.builder()
                            .tableName(transferTrackerDbTableName)
                            .key(key)
                            .build()).item();
                    assertThat(dbClientItem.get("state").s()).isEqualTo("ACTION:EHR_TRANSFER_TIMEOUT");
                    assertThat(dbClientItem.get("conversation_id").s()).isEqualTo("conversationId-for-timeout-integration-test");
                 //   checkMessageInRelatedQueue(transferCompleteQUrl);
                });
   }

//    private List<Message> checkMessageInRelatedQueue(String queueUrl) {
//        System.out.println("checking sqs queue: " + queueUrl);
//
//        var requestForMessagesWithAttributes
//                = new ReceiveMessageRequest().withQueueUrl(queueUrl);
//        var messages = sqs.receiveMessage(requestForMessagesWithAttributes).getMessages();
//        System.out.println("messages in checkMessageInRelatedQueue: " + messages);
//        assertThat(messages).hasSize(1);
//        return messages;
//    }

    private String getRepoIncomingData() {
        return "{ \"conversationId\" : \"" + CONVERSATION_ID + "\",\n \"nhsNumber\" : \"" + NHS_NUMBER + "\",\n \"nemsMessageId\" : \"" + NEMS_MESSAGE_ID + "\",\n \"sourceGp\": \"" + SOURCE_GP + "\",\n \"destinationGp\": \"D3ST1NAT10N\" ,\n \"nemsEventLastUpdated\": \"" + NEMS_EVENT_LAST_UPDATED + "\"}";
    }
}
