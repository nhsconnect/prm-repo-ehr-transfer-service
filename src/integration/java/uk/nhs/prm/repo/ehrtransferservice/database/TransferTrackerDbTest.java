package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest()
@ContextConfiguration(classes = {LocalStackAwsConfig.class})
public class TransferTrackerDbTest {

    @Autowired
    TransferTrackerDb transferTrackerDb;

    String conversationId = "conversation Id";
    String nhsNumber = "111111111";
    String sourceGP = "source gp";
    String nemsMessageId = "Nems message Id";
    String state = "state";
    String dateTime = "2017-11-01T15:00:33+00:00";

    @BeforeEach
    public void setUp() {
        transferTrackerDb.save(new TransferTrackerDbEntry(conversationId, nhsNumber, sourceGP, nemsMessageId, state, dateTime));
    }

    @Test
    void shouldReadFromDb() {
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
    }

    @Test
    void shouldUpdateRecord() {
        var newTimestamp = "2018-11-01T15:00:33+00:00";
        transferTrackerDb.save(new TransferTrackerDbEntry(conversationId, nhsNumber, sourceGP, nemsMessageId, state, newTimestamp));
        var transferTrackerDbData = transferTrackerDb.getByConversationId(conversationId);
        assertThat(transferTrackerDbData.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(transferTrackerDbData.getConversationId()).isEqualTo(conversationId);
        assertThat(transferTrackerDbData.getDateTime()).isEqualTo(newTimestamp);
    }

    @Test
    void shouldHandleConversationIdThatDoesNotExistInDb() {
        var notExistingConversationId = "non-existing conversation Id";
        var lastUpdatePatientData = transferTrackerDb.getByConversationId(notExistingConversationId);
        assertThat(lastUpdatePatientData).isEqualTo(null);
    }
}