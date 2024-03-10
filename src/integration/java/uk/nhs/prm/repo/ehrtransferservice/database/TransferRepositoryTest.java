package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus.EHR_TRANSFER_STARTED;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { LocalStackAwsConfig.class })
@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
public class TransferRepositoryTest {
    @Autowired
    TransferRepository transferRepository;

    @Autowired
    private DynamoDbClient dbClient;

    @Value("${aws.transferTrackerDbTableName}")
    private String transferTrackerDbTableName;

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String DESTINATION_GP = "A74854";
    private static final String CONVERSATION_ID = "44635df1-d18a-4a77-8256-f5ff21289664";
    private static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";


    /**
     * TODO: CREATE CONVERSATION - HAPPY PATH
     * TODO: CREATE CONVERSATION - UNHAPPY PATH
     */

    @Test
    void createConversation_ValidRepoIncomingEvent_ShouldPersist() {
        // given
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent();
        final UUID inboundConversationId = UUID.fromString(CONVERSATION_ID);

        // when
        transferRepository.createConversation(repoIncomingEvent);
        final ConversationRecord record = transferRepository
            .findConversationByInboundConversationId(inboundConversationId);

        final String resultDestinationGp = record.destinationGp().orElseThrow();

        // then
        assertAll(
            () -> assertEquals(record.inboundConversationId().toString(), CONVERSATION_ID),
            () -> assertEquals(record.nhsNumber(), NHS_NUMBER),
            () -> assertEquals(record.sourceGp(), SOURCE_GP),
            () -> assertEquals(resultDestinationGp, DESTINATION_GP), // TODO - Add logic to update this.
            () -> assertEquals(record.state(), EHR_TRANSFER_STARTED.name()),
            () -> assertEquals(record.failureCode(), Optional.empty()),
            () -> assertEquals(record.nemsMessageId().toString(), NEMS_MESSAGE_ID)
        );
    }



    // Helper Methods
    private RepoIncomingEvent createRepoIncomingEvent() {
        return new RepoIncomingEvent(
            NHS_NUMBER,
            SOURCE_GP,
            NEMS_MESSAGE_ID,
            DESTINATION_GP,
            NEMS_EVENT_LAST_UPDATED,
            CONVERSATION_ID
        );
    }
}