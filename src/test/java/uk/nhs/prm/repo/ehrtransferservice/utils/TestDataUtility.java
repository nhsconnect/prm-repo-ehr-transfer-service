package uk.nhs.prm.repo.ehrtransferservice.utils;

import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.UUID;

public final class TestDataUtility {
    private TestDataUtility() { }

    public static final String NHS_NUMBER = "9798547485";
    public static final String SOURCE_GP = "B45744";
    public static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    public static final String DESTINATION_GP = "A74854";
    public static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";
    public static final String REPOSITORY_ASID = "REPOSITORY";

    public static RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
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