package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class EhrRepoServiceTest {
    EhrRepoService ehrRepoService;

    @Mock
    EhrRepoClient mockEhrRepoClient;

    @BeforeEach
    void setUp() {
        ehrRepoService = new EhrRepoService(mockEhrRepoClient);
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage() throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(new byte[10]);
        message.reset();
        return message;
    }

    @Test
    void shouldCoordinateEhrRepoClientCalls() throws MalformedURLException, URISyntaxException, HttpException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        PresignedUrl mockPresignedUrl = mock(PresignedUrl.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenReturn(mockPresignedUrl);

        ehrRepoService.storeMessage(mockParsedMessage);
        verify(mockEhrRepoClient, times(1)).fetchStorageUrl(conversationId, messageId);
        verify(mockPresignedUrl, times(1)).uploadMessage(mockParsedMessage);
        verify(mockEhrRepoClient, times(1)).confirmMessageStored(mockParsedMessage);
    }

    @Test
    void shouldThrowStorageFailureExceptionWhenPresignedUrlCannotBeRetrieved() throws MalformedURLException, URISyntaxException, HttpException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenThrow(new HttpException());


        Exception expected = assertThrows(HttpException.class, () ->
                ehrRepoService.storeMessage(mockParsedMessage)
        );
        assertThat(expected, notNullValue());
    }

    @Test
    void shouldThrowStorageFailureExceptionWhenCannotStoreMessage() throws MalformedURLException, URISyntaxException, HttpException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        PresignedUrl mockPresignedUrl = mock(PresignedUrl.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenReturn(mockPresignedUrl);
        doThrow(new HttpException()).when(mockEhrRepoClient).confirmMessageStored(any());


        Exception expected = assertThrows(HttpException.class, () ->
                ehrRepoService.storeMessage(mock(ParsedMessage.class))
        );
        assertThat(expected, notNullValue());
    }
}
