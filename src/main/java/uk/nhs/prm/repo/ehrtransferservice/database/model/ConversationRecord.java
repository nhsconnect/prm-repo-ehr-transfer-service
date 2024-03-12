package uk.nhs.prm.repo.ehrtransferservice.database.model;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public record ConversationRecord(
    UUID inboundConversationId,
    Optional<UUID> outboundConversationId,
    String nhsNumber,
    String sourceGp,
    Optional<String> destinationGp,
    String state,
    Optional<String> failureCode,
    Optional<UUID> nemsMessageId,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt,
    Optional<Instant> deletedAt
) { }