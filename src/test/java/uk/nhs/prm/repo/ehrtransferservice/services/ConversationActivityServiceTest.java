package uk.nhs.prm.repo.ehrtransferservice.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationActivityServiceTest {
    private static final UUID INBOUND_CONVERSATION_ID = UUID.randomUUID();

    private final ConversationActivityService activityService;

    public ConversationActivityServiceTest() {
        this.activityService = new ConversationActivityService();
    }

    @AfterEach
    void afterEach() {
        activityService.removeConversationActivityTimestamp(INBOUND_CONVERSATION_ID);
    }

    @Test
    void captureConversationActivityTimestamp_ValidInboundConversationId_Ok() {
        // when
        activityService.captureConversationActivityTimestamp(INBOUND_CONVERSATION_ID);

        // then
        assertTrue(activityService.isConversationActive(INBOUND_CONVERSATION_ID));
    }

    @Test
    void removeConversationActivityTimestamp_ValidInboundConversationId_Ok() {
        // when
        activityService.captureConversationActivityTimestamp(INBOUND_CONVERSATION_ID);
        activityService.removeConversationActivityTimestamp(INBOUND_CONVERSATION_ID);

        // then
        assertFalse(activityService.isConversationActive(INBOUND_CONVERSATION_ID));
    }

    @Test
    void isConversationActive_ActiveInboundConversationId_ReturnTrue() {
        // when
        activityService.captureConversationActivityTimestamp(INBOUND_CONVERSATION_ID);
        final boolean result = activityService.isConversationActive(INBOUND_CONVERSATION_ID);

        // then
        assertTrue(result);
    }

    @Test
    void isConversationActive_InactiveInboundConversationId_ReturnFalse() {
        // when
        final boolean result = activityService.isConversationActive(INBOUND_CONVERSATION_ID);

        // then
        assertFalse(result);
    }

    @Test
    void isConversationTimedOut_ActiveInboundConversationId_ReturnTrue() {
        // when
        activityService.captureConversationActivityTimestamp(INBOUND_CONVERSATION_ID);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            final boolean result = activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID, 5);
            assertTrue(result);
        });
    }

    @Test
    void isConversationTimedOut_ActiveInboundConversationId_ReturnFalse() {
        // when
        activityService.captureConversationActivityTimestamp(INBOUND_CONVERSATION_ID);
        final boolean result = activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID, 10);

        // then
        assertFalse(result);
    }
}