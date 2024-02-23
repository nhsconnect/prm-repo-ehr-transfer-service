package uk.nhs.prm.repo.ehrtransferservice.database.model;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record MessageRecord(
    UUID inboundConversationId,
    Optional<UUID> outboundConversationId,
    String state,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Optional<LocalDateTime> deletedAt
) { }