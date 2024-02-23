package uk.nhs.prm.repo.ehrtransferservice.database.model;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record ConversationRecord(
    UUID inboundConversationId,
    Optional<UUID> outboundConversationId,
    String nhsNumber,
    String sourceGp,
    String destinationGp,
    String state,
    Optional<UUID> meshMessageId,
    Optional<UUID> nemsMessageId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }