package uk.nhs.prm.repo.ehrtransferservice.database.model;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record ConversationRecord(
    UUID inboundConversationId,
    String nhsNumber,
    String sourceGp,
    Optional<String> destinationGp,
    String state,
    Optional<String> failureCode,
    Optional<UUID> nemsMessageId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }