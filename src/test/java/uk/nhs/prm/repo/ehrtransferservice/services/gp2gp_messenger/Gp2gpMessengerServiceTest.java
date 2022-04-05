package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.ConversationIdStore;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class Gp2gpMessengerServiceTest {
    @Mock
    Gp2gpMessengerClient gp2gpMessengerClient;
    @Mock
    ConversationIdStore conversationIdStore;
    @InjectMocks
    Gp2gpMessengerService gp2gpMessengerService;

    @BeforeEach
    void setUp() {
        setField(gp2gpMessengerService, "repositoryAsid", "some-asid");
    }

    @Test
    void shouldCallGp2gpMessengerForEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();
        when(conversationIdStore.getConversationId()).thenReturn("randomUUID");

        gp2gpMessengerService.sendEhrRequest(incomingEvent);

        Gp2gpMessengerEhrRequestBody gp2gpMessengerEhrRequestBody =
                new Gp2gpMessengerEhrRequestBody("destination-gp", "some-asid", "source-gp", "randomUUID");
        verify(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest("123456765", gp2gpMessengerEhrRequestBody);
    }

    @Test
    void shouldThrowHttpExceptionWhenWeGotAnyStatusCodeButNot204() throws HttpException, URISyntaxException, IOException, InterruptedException {
        var incomingEvent = createIncomingEvent();
        when(conversationIdStore.getConversationId()).thenReturn("randomUUID");

        doThrow(new HttpException()).when(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest(any(), any());

        Assertions.assertThrows(Exception.class, () -> {
            gp2gpMessengerService.sendEhrRequest(incomingEvent);
        });

    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp");
    }

}