package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class Gp2gpMessengerServiceTest {
    @Mock
    Gp2gpMessengerClient gp2gpMessengerClient;
    @Mock
    ParsedMessage parsedMessage;
    @InjectMocks
    Gp2gpMessengerService gp2gpMessengerService;

    @BeforeEach
    void setUp() {
        setField(gp2gpMessengerService, "repositoryAsid", "some-asid");
    }

    @Test
    void shouldCallGp2gpMessengerForEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();

        gp2gpMessengerService.sendEhrRequest(incomingEvent);

        Gp2gpMessengerEhrRequestBody gp2gpMessengerEhrRequestBody =
                new Gp2gpMessengerEhrRequestBody("destination-gp", "some-asid", "source-gp", "randomUUID");
        verify(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest("123456765", gp2gpMessengerEhrRequestBody);
    }

    @Test
    void shouldThrowHttpExceptionWhenWeGotAnyStatusCodeButNot204() throws HttpException, URISyntaxException, IOException, InterruptedException {
        var incomingEvent = createIncomingEvent();

        doThrow(new HttpException()).when(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest(any(), any());

        Assertions.assertThrows(Exception.class, () -> gp2gpMessengerService.sendEhrRequest(incomingEvent));
    }

    @Test
    void shouldCallGp2GpMessengerForContinueRequest() {
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getOdsCode()).thenReturn("ods-code");
        gp2gpMessengerService.sendContinueMessage(parsedMessage);
    }

    @Test
    @Disabled("WIP, this test shall be enabled when send continue functionality is plugged correctly")
    void shouldCatchExceptionWhenThrownFromClient() {
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getOdsCode()).thenReturn("ods-code");
        assertThrows(Exception.class, () -> gp2gpMessengerService.sendContinueMessage(parsedMessage));
    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp", "randomUUID");
    }

}