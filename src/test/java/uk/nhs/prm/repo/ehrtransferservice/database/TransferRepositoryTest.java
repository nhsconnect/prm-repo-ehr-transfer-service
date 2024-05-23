package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationUpdateException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.INBOUND_CONVERSATION_ID;

@ExtendWith(MockitoExtension.class)
class TransferRepositoryTest {
    @Mock
    private AppConfig appConfig;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private TransferRepository transferRepository;

    private static final String DATABASE_NAME = "database";
    private static final ArgumentCaptor<UpdateItemRequest> updateItemRequestCaptor = ArgumentCaptor.forClass(UpdateItemRequest.class);

    @BeforeEach
    void beforeEach() {
        doReturn(DATABASE_NAME)
                .when(appConfig)
                .transferTrackerDbTableName();
    }

    @Test
    void updateConversationStatus_ValidInboundConversationIdAndStatus_ShouldCallUpdateItem() {
        // given
        final String inboundConversationId = "F68FE779-5A2A-490B-81FE-6976543EFC06";
        final ConversationTransferStatus status = ConversationTransferStatus.INBOUND_COMPLETE;
        final String conditionExpression = "attribute_exists(CreatedAt)";

        // when
        transferRepository.updateConversationStatus(UUID.fromString(inboundConversationId), status);

        // then
        verify(appConfig).transferTrackerDbTableName();
        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(conditionExpression, updateItemRequestCaptor.getValue().conditionExpression());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
        assertEquals(DATABASE_NAME, updateItemRequestCaptor.getValue().tableName());
    }

    @Test
    void updateConversationStatus_InvalidInboundConversationIdAndValidStatus_ShouldThrowConversationUpdateException() {
        // given
        final String inboundConversationId = "C68082F9-EFB9-4144-BAA0-3A2F2E2A88B9";
        final ConversationTransferStatus status = ConversationTransferStatus.INBOUND_COMPLETE;

        // when
        doThrow(ConditionalCheckFailedException.class)
                .when(dynamoDbClient)
                .updateItem(any(UpdateItemRequest.class));

        // then
        assertThrows(
                ConversationUpdateException.class,
                () -> transferRepository.updateConversationStatus(UUID.fromString(inboundConversationId), status)
        );

        verify(appConfig).transferTrackerDbTableName();
        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
    }
}