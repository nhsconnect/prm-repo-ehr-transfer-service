package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_STARTED;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { LocalStackAwsConfig.class })
@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
public class TransferServiceTest {
    @Autowired
    TransferService transferService;

    @Autowired
    TransferTrackerDbUtility transferTrackerDbUtility;

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String DESTINATION_GP = "A74854";
    private static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";
    private static final String EHR_CORE_MESSAGE_ID = "13cd1199-4b3a-44dc-9a60-6abcc22b8a44";

    @Test
    void createConversation_ValidRepoIncomingEvent_ShouldCreateConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // when
        transferService.createConversation(repoIncomingEvent);
        ConversationRecord record = transferService
            .getConversationByInboundConversationId(inboundConversationId);

        String nemsMessageIdResult = record.nemsMessageId()
            .orElseThrow()
            .toString();

        // then
        assertEquals(record.inboundConversationId().toString(), inboundConversationId.toString());
        assertEquals(record.nhsNumber(), NHS_NUMBER);
        assertEquals(record.sourceGp(), SOURCE_GP);
        assertEquals(record.state(), INBOUND_STARTED.name());
        assertEquals(record.failureCode(), Optional.empty());
        assertEquals(nemsMessageIdResult, NEMS_MESSAGE_ID);
        assertNotNull(record.createdAt());
        assertNotNull(record.updatedAt());
    }
//
//    @Test
//    void isInboundConversationPresent_ValidInboundConversationId_ShouldReturnTrue() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
//
//        // when
//        transferRepository.createConversation(event);
//        boolean isConversationPresent = transferRepository
//            .isInboundConversationPresent(inboundConversationId);
//
//        // then
//        assertTrue(isConversationPresent);
//    }
//
//    @Test
//    void isInboundConversationPresent_NonExistingInboundConversationId_ShouldReturnFalse() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//
//        // when
//        boolean isConversationPresent = transferRepository
//            .isInboundConversationPresent(inboundConversationId);
//
//        // then
//        assertFalse(isConversationPresent);
//    }
//
//    @Test
//    void findConversationByInboundConversationId_NonExistingInboundConversationId_ShouldThrowTransferRecordNotPresentException() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final String exceptionMessage = "No transfer present for Inbound Conversation ID %s";
//
//        // when
//        final DatabaseException exception = assertThrows(TransferRecordNotPresentException.class,
//            () -> transferRepository.findConversationByInboundConversationId(inboundConversationId));
//
//        // then
//        assertEquals(exception.getMessage(), exceptionMessage.formatted(inboundConversationId));
//    }
//
//    @Test
//    void updateConversationStatus_ValidInboundConversationIdAndConversationTransferStatus_ShouldUpdateStatus() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
//
//        // when
//        transferRepository.createConversation(event);
//        transferRepository.updateConversationStatus(inboundConversationId, INBOUND_FAILED);
//        ConversationRecord record = transferRepository
//            .findConversationByInboundConversationId(inboundConversationId);
//
//        // then
//        assertEquals(record.state(), INBOUND_FAILED.name());
//    }
//
//    @Test
//    void updateConversationStatus_NonExistingInboundConversationIdAndConversationTransferStatus_ShouldThrowUpdateConversationException() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final String exceptionMessage = "The conversation could not be updated with Inbound Message ID %s";
//
//        // when
//        final DatabaseException exception = assertThrows(UpdateConversationException.class, () ->
//            transferRepository.updateConversationStatus(inboundConversationId, INBOUND_FAILED));
//
//        // then
//        assertEquals(exception.getMessage(), exceptionMessage.formatted(inboundConversationId));
//    }
//
//    @Test
//    void updateConversationStatusWithFailure_ValidInboundConversationIdAndFailureCode_ShouldUpdateFailureCode() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
//        final String failureCode = "19";
//
//        // when
//        transferRepository.createConversation(event);
//        transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode);
//        final ConversationRecord record = transferRepository
//            .findConversationByInboundConversationId(inboundConversationId);
//
//        final String failureCodeResult = record.failureCode().orElseThrow();
//
//        // then
//        assertEquals(record.state(), INBOUND_FAILED.name());
//        assertEquals(failureCodeResult, failureCode);
//    }
//
//    @Test
//    void updateConversationStatusWithFailure_NonExistingInboundConversationIdAndFailureCode_ShouldThrowUpdateConversationException() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final String failureCode = "19";
//        final String exceptionMessage = "The conversation could not be updated with Inbound Message ID %s";
//
//        // when
//        final DatabaseException exception = assertThrows(UpdateConversationException.class, () ->
//            transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode));
//
//        // then
//        assertEquals(exception.getMessage(), exceptionMessage.formatted(inboundConversationId));
//    }
//
//    @Test
//    void getEhrCoreInboundMessageIdForInboundConversationId_ValidInboundConversationId_ShouldReturnMessageId() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final UUID ehrCoreMessageId = UUID.fromString(EHR_CORE_MESSAGE_ID);
//
//        // when
//        transferTrackerDbUtility.createCore(inboundConversationId, ehrCoreMessageId);
//
//        final UUID result =
//            transferRepository.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);
//
//        // then
//        assertEquals(result, ehrCoreMessageId);
//    }
//
//    @Test
//    void getEhrCoreInboundMessageIdForInboundConversationId_NonExistingInboundConversationId_ShouldThrowQueryReturnedNoItemsException() {
//        // given
//        final UUID inboundConversationId = UUID.randomUUID();
//        final String exceptionMessage = "The query returned no items for Inbound Conversation ID %s";
//
//        // when
//        final DatabaseException exception = assertThrows(QueryReturnedNoItemsException.class,
//            () -> transferRepository.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId));
//
//        // then
//        assertEquals(exception.getMessage(), exceptionMessage.formatted(inboundConversationId));
//    }
//
    // Helper Methods
    private RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
            .nhsNumber(NHS_NUMBER)
            .sourceGp(SOURCE_GP)
            .nemsMessageId(NEMS_MESSAGE_ID)
            .destinationGp(DESTINATION_GP)
            .nemsEventLastUpdated(NEMS_EVENT_LAST_UPDATED)
            .conversationId(inboundConversationId.toString())
            .build();
    }
}