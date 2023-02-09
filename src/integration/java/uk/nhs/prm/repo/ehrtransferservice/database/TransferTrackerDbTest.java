package uk.nhs.prm.repo.ehrtransferservice.database;

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
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest()
@ContextConfiguration(classes = {LocalStackAwsConfig.class})
public class TransferTrackerDbTest {

    @Autowired
    TransferTrackerDb transferTrackerDb;

    @Autowired
    private DynamoDbClient dbClient;

    String conversationId = "conversation Id";
    String nhsNumber = "111111111";
    String sourceGP = "source gp";
    String nemsMessageId = "Nems message Id";
    String nemsEventLastUpdated = "Last updated";
    String state = "state";
    String createdAt = "2017-11-01T15:00:33+00:00";
    String lastUpdatedAt = "2017-11-01T15:00:33+00:00";
    String largeEhrCoreMessageId = "large ehr core message Id";
    Boolean active = true;


    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    @BeforeEach
    public void setUp() {
        transferTrackerDb.save(new Transfer(conversationId, nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, createdAt, lastUpdatedAt, largeEhrCoreMessageId, active));
    }

    @AfterEach
    void tearDown() {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());
        dbClient.deleteItem(DeleteItemRequest.builder().tableName(transferTrackerDbTableName).key(key).build());
    }

    @Test
    void shouldReadFromDb() {
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
    }

    @Test
    void shouldDoInitialUpdateOfRecord() {
        var newTimestamp = "2018-11-01T15:00:33+00:00";
        transferTrackerDb.save(new Transfer(conversationId, nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, newTimestamp, newTimestamp, largeEhrCoreMessageId, active));
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
        assertThat(transferTrackerDbData.getLastUpdatedAt()).isEqualTo(newTimestamp);
        assertThat(transferTrackerDbData.getCreatedAt()).isEqualTo(transferTrackerDbData.getLastUpdatedAt());
    }

    @Test
    void shouldHandleConversationIdThatDoesNotExistInDb() {
        var notExistingConversationId = "non-existing conversation Id";
        var lastUpdatePatientData = transferTrackerDb.getByConversationId(notExistingConversationId);
        assertThat(lastUpdatePatientData).isEqualTo(null);
    }

    @Test
    void shouldUpdateOnlyStateAndLastUpdatedAt() {
        var newTimestamp = "2222-11-01T15:00:33+00:00";
        transferTrackerDb.update(conversationId, "ACTION:EHR_REQUEST_SENT", newTimestamp, true);

        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getState()).isEqualTo("ACTION:EHR_REQUEST_SENT");
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
        assertThat(transferTrackerDbData.getLastUpdatedAt()).isEqualTo(newTimestamp);
    }

    @Test
    void shouldUpdateOnlyLargeEhrCoreMessageId() {
        String updatedLargeEhrCoreMessageId = "updated-large-ehr-core-message-id";
        transferTrackerDb.updateLargeEhrCoreMessageId(conversationId, updatedLargeEhrCoreMessageId);

        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getLargeEhrCoreMessageId()).isEqualTo(updatedLargeEhrCoreMessageId);
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
    }

    @Test
    void shouldCreateDbRecordWithIsActiveValue() {
        var newTimestamp = "2018-11-01T15:00:33+00:00";
        transferTrackerDb.save(new Transfer(conversationId, nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, newTimestamp, newTimestamp, largeEhrCoreMessageId, active));
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getIsActive()).isTrue();
    }

    @Test
    void shouldRemoveIsActiveAttributeOnceTransferIsComplete() {
        var newTimestamp = "2222-11-01T15:00:33+00:00";
        transferTrackerDb.update(conversationId, "ACTION:EHR_TRANSFER_TO_REPO_COMPLETE", newTimestamp, false);
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getIsActive()).isFalse();
        assertThat(transferTrackerDbData.getLastUpdatedAt()).isEqualTo(newTimestamp);
    }

    @Test
    void shouldNotReturnTheRecordWhenItsCreatedAtDateHasNotPassedTimeOutPeriod (){
        String timeStampBeforeTimeOut = "2022-08-10T12:27:27.640260Z";
        transferTrackerDb.save(new Transfer("another-conversationId", nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, timeStampBeforeTimeOut, timeStampBeforeTimeOut, largeEhrCoreMessageId, true));
        String timeOutTimeStamp = "2022-08-10T12:14:17.640260Z";
        List<Transfer> transferTrackerDbData = transferTrackerDb.getTimedOutRecords(timeOutTimeStamp);
        assertThat(transferTrackerDbData.size()).isEqualTo(1); // This record is from @BeforeEach
        assertThat(transferTrackerDbData.get(0).getConversationId()).isEqualTo(conversationId);
    }

    @Test
    void shouldNotReturnTheRecordWhenItsCreatedAtDateHasPassedTimeOutPeriodButIsActiveIsFalse (){
        String timeStampBeforeTimeOut = "2022-08-10T12:01:27.640260Z";
        transferTrackerDb.save(new Transfer("another-conversationId", nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, timeStampBeforeTimeOut, timeStampBeforeTimeOut, largeEhrCoreMessageId, false));
        String timeOutTimeStamp = "2022-08-10T12:14:17.640260Z";
        List<Transfer> transferTrackerDbData = transferTrackerDb.getTimedOutRecords(timeOutTimeStamp);
        assertThat(transferTrackerDbData.size()).isEqualTo(1);
        assertThat(transferTrackerDbData.get(0).getConversationId()).isEqualTo(conversationId);
    }

    @Test
    void shouldReturnTheRecordWhenItsCreatedAtDateHasPassedTimeOutPeriodAndIsActiveIsTrue (){
        String timeStampBeforeTimeOut = "2022-08-10T12:01:27.640260Z";
        transferTrackerDb.save(new Transfer("another-conversationId", nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, timeStampBeforeTimeOut, timeStampBeforeTimeOut, largeEhrCoreMessageId, true));
        String timeOutTimeStamp = "2022-08-10T12:14:17.640260Z";
        List<Transfer> transferTrackerDbData = transferTrackerDb.getTimedOutRecords(timeOutTimeStamp);
        assertThat(transferTrackerDbData.size()).isEqualTo(2);
        assertThat(transferTrackerDbData.get(1).getConversationId()).isEqualTo("another-conversationId");
    }
}