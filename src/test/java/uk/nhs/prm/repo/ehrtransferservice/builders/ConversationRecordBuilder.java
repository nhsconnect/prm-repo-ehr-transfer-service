package uk.nhs.prm.repo.ehrtransferservice.builders;

import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus.EHR_TRANSFER_STARTED;

public class ConversationRecordBuilder {

    private UUID inboundConversationId;

    private String nhsNumber;

    private String sourceGp;

    private Optional<String> destinationGp;

    private String state;

    private Optional<String> failureCode;

    private Optional<UUID> nemsMessageId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ConversationRecordBuilder withDefaults() {
        inboundConversationId = UUID.fromString("9b1bf808-353f-4459-a325-2ded5c576e63");
        nhsNumber = "1234567890";
        sourceGp = "ODSCODE";
        destinationGp = Optional.empty();
        state = EHR_TRANSFER_STARTED.name();
        failureCode = Optional.empty();
        nemsMessageId = Optional.of(UUID.fromString("d221cbca-7e76-4eac-83b6-98dcc615dd60"));
        createdAt = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        updatedAt = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        return this;
    }

    public ConversationRecordBuilder withInboundConversationId(UUID inboundConversationId) {
        this.inboundConversationId = inboundConversationId;
        return this;
    }

    public ConversationRecord build() {
        return new ConversationRecord(
                inboundConversationId,
                nhsNumber,
                sourceGp,
                destinationGp,
                state,
                failureCode,
                nemsMessageId,
                createdAt,
                updatedAt
        );
    }
}
