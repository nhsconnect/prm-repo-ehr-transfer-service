package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.services.Gp2gpMessengerClient;
import uk.nhs.prm.repo.ehrtransferservice.services.HttpException;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferTrackerService transferTrackerService;

    @InjectMocks
    RepoIncomingService repoIncomingService;

    @Mock
    Gp2gpMessengerClient gp2gpMessengerClient;

    @Mock
    ConversationIdStore conversationIdStore;

    @BeforeEach
    void setUp(){
        setField(repoIncomingService, "repositoryAsid", "some-asid");
    }

    @Test
    void shouldParseIncomingEventMessage() throws HttpException, IOException, URISyntaxException, InterruptedException {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferTrackerService).recordEventInDb(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");

    }

    @Test
    void shouldCallGp2gpMessengerForEhrRequest() throws HttpException, IOException, URISyntaxException, InterruptedException {
        var incomingEvent = createIncomingEvent();
        when(conversationIdStore.getConversationId()).thenReturn("randomUUID");

        repoIncomingService.processIncomingEvent(incomingEvent);

        Gp2gpMessengerEhrRequestBody gp2gpMessengerEhrRequestBody =
                new Gp2gpMessengerEhrRequestBody("destination-gp", "some-asid", "source-gp","randomUUID");
        verify(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest("123456765",gp2gpMessengerEhrRequestBody );
    }


    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp");
    }
}