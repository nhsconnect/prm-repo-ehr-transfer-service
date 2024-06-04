package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.QueryReturnedNoItemsException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { LocalStackAwsConfig.class })
@ExtendWith(ForceXercesParserExtension.class)
public class TransferServiceTest {
    @Autowired
    TransferService transferService;

    @Autowired
    TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    ConversationActivityService conversationActivityService;

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String DESTINATION_GP = "A74854";
    private static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";
    private static final String EHR_CORE_MESSAGE_ID = "13CD1199-4B3A-44DC-9A60-6ABCC22B8A44";


    @Test
    void createConversation_ValidRepoIncomingEvent_ShouldCreateOrRetryConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // when
        transferService.createOrRetryConversation(repoIncomingEvent);
        ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);

        String nemsMessageIdResult = record.nemsMessageId()
                .orElseThrow()
                .toString();

        // then
        assertEquals(record.inboundConversationId().toString(), inboundConversationId.toString());
        assertEquals(record.nhsNumber(), NHS_NUMBER);
        assertEquals(record.sourceGp(), SOURCE_GP);
        assertEquals(record.transferStatus(), INBOUND_STARTED);
        assertEquals(record.failureCode(), Optional.empty());
        assertEquals(nemsMessageIdResult, NEMS_MESSAGE_ID);
        assertNotNull(record.createdAt());
        assertNotNull(record.updatedAt());
    }

    @Test
    void isInboundConversationPresent_ValidInboundConversationId_ShouldReturnTrue() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);

        // when
        transferService.createOrRetryConversation(event);
        boolean isConversationPresent = transferService
            .isInboundConversationPresent(inboundConversationId);

        // then
        assertTrue(isConversationPresent);
    }

    @Test
    void isInboundConversationPresent_NonExistingInboundConversationId_ShouldReturnFalse() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        boolean isConversationPresent = transferService
            .isInboundConversationPresent(inboundConversationId);

        // then
        assertFalse(isConversationPresent);
    }

    @Test
    void getConversationByInboundConversationId_NonExistingInboundConversationId_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        assertThrows(ConversationNotPresentException.class,
            () -> transferService.getConversationByInboundConversationId(inboundConversationId));
    }

    @Test
    void updateConversationTransferStatus_ValidInboundConversationIdAndConversationTransferStatus_ShouldUpdateStatus() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);

        // when
        transferService.createOrRetryConversation(event);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_FAILED);
        ConversationRecord record = transferService
            .getConversationByInboundConversationId(inboundConversationId);

        // then
        assertEquals(record.transferStatus(), INBOUND_FAILED);
    }

    @Test
    void updateConversationTransferStatus_NonExistingInboundConversationIdAndExistingConversationTransferStatus_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // then
        assertThrows(ConversationNotPresentException.class, () ->
                transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_FAILED));
    }

    @Test
    void updateConversationTransferStatusWithFailure_ValidInboundConversationIdAndFailureCode_ShouldUpdateFailureCode() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
        final String failureCode = "19";

        // when
        transferService.createOrRetryConversation(event);
        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);
        final ConversationRecord record = transferService
            .getConversationByInboundConversationId(inboundConversationId);

        final String failureCodeResult = record.failureCode().orElseThrow();

        // then
        assertEquals(record.transferStatus(), INBOUND_FAILED);
        assertEquals(failureCodeResult, failureCode);
    }

    @Test
    void updateConversationTransferStatusWithFailure_NonExistingInboundConversationIdAndFailureCode_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final String failureCode = "19";

        // when
        assertThrows(ConversationNotPresentException.class, () ->
            transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode));
    }

    @Test
    void getEhrCoreInboundMessageIdForInboundConversationId_ValidInboundConversationId_ShouldReturnMessageId() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final UUID ehrCoreMessageId = UUID.fromString(EHR_CORE_MESSAGE_ID);

        // when
        transferTrackerDbUtility.createCore(inboundConversationId, ehrCoreMessageId);

        final UUID result =
            transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        // then
        assertEquals(result, ehrCoreMessageId);
    }

    @Test
    void getEhrCoreInboundMessageIdForInboundConversationId_NonExistingInboundConversationId_ShouldThrowQueryReturnedNoItemsException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        assertThrows(QueryReturnedNoItemsException.class,
            () -> transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId));
    }

    @Test
    void isConversationIneligibleForRetry_ConversationIsActiveAndNotTimedOut_ShouldThrowConversationAlreadyActiveException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        conversationActivityService.captureConversationActivity(inboundConversationId);

        // then
        assertTrue(conversationActivityService.isConversationActive(inboundConversationId));
        assertThrows(ConversationIneligibleForRetryException.class,
                () -> transferService.verifyIfConversationIneligibleForRetry(inboundConversationId));
    }

    @Test
    void isConversationIneligibleForRetry_NonExistentInboundConversationId_ShouldReturnTrue() throws ConversationIneligibleForRetryException {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        boolean isConversationEligibleForRetry = transferService.verifyIfConversationIneligibleForRetry(inboundConversationId);

        // then
        assertTrue(isConversationEligibleForRetry);
    }

    @Test
    void isConversationIneligibleForRetry_TransferStatusInboundStarted_ShouldReturnTrue() throws ConversationIneligibleForRetryException {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
        transferService.createOrRetryConversation(event);
        final ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);
        final String transferStatus = record.state();

        // when
        boolean isConversationEligibleForRetry = transferService.verifyIfConversationIneligibleForRetry(inboundConversationId);

        // then
        assertEquals(transferStatus, INBOUND_STARTED.name());
        assertTrue(isConversationEligibleForRetry);
    }

    @Test
    void isConversationIneligibleForRetry_TransferStatusInboundTimeout_ShouldReturnTrue() throws ConversationIneligibleForRetryException {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
        transferService.createOrRetryConversation(event);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_TIMEOUT);
        final ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);
        final String transferStatus = record.state();

        // when
        boolean isConversationEligibleForRetry = transferService.verifyIfConversationIneligibleForRetry(inboundConversationId);

        // then
        assertEquals(transferStatus, INBOUND_TIMEOUT.name());
        assertTrue(isConversationEligibleForRetry);
    }

    // Helper Methods
    private RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
            .nhsNumber(NHS_NUMBER)
            .sourceGp(SOURCE_GP)
            .nemsMessageId(NEMS_MESSAGE_ID)
            .destinationGp(DESTINATION_GP)
            .nemsEventLastUpdated(NEMS_EVENT_LAST_UPDATED)
            .conversationId(inboundConversationId.toString().toUpperCase())
            .build();
    }
}