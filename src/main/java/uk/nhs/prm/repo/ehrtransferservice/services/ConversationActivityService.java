package uk.nhs.prm.repo.ehrtransferservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class ConversationActivityService {
    private final Map<UUID, Instant> conversations;

    @Value("${inboundTimeoutSeconds}")
    private int inboundTimeoutSeconds;

    public ConversationActivityService() {
        this.conversations = new ConcurrentHashMap<>();
    }

    public void captureConversationActivityTimestamp(UUID inboundConversationId) {
        conversations.put(inboundConversationId, Instant.now());
    }

    public void removeConversationActivityTimestamp(UUID inboundConversationId) {
        conversations.remove(inboundConversationId);
    }

    public boolean isConversationActive(UUID inboundConversationId) {
        return conversations.containsKey(inboundConversationId);
    }

    public boolean isConversationTimedOut(UUID inboundConversationId) {
        return conversations.get(inboundConversationId)
            .plus(inboundTimeoutSeconds, ChronoUnit.SECONDS)
            .isBefore(Instant.now());
    }
}
