package uk.nhs.prm.repo.ehrtransferservice.utility;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class InboundTimeoutTracker {
    private static final Map<UUID, Instant> CONVERSATION_ACTIVITY;
    private InboundTimeoutTracker() { }

    static {
        CONVERSATION_ACTIVITY = new ConcurrentHashMap<>();
    }

    public static void captureConversationActivityTimestamp(UUID inboundConversationId) {
        final Instant now = Instant.now();
        log.info("Capturing inbound timeout activity for InboundConversationId: {}, current timestamp is: {}",
            inboundConversationId, now);
        CONVERSATION_ACTIVITY.put(inboundConversationId, now);
    }

    public static void removeConversationActivityTimestamp(UUID inboundConversationId) {
        log.info("Activity removed for Inbound Conversation ID {}", inboundConversationId);
        CONVERSATION_ACTIVITY.remove(inboundConversationId);
    }

    public static boolean isConversationActive(UUID inboundConversationId) {
        return CONVERSATION_ACTIVITY.containsKey(inboundConversationId);
    }

    public static boolean isConversationTimedOut(UUID inboundConversationId, int inboundSeconds) {
        return CONVERSATION_ACTIVITY.get(inboundConversationId)
            .plus(inboundSeconds, ChronoUnit.SECONDS)
            .isBefore(Instant.now());
    }
}