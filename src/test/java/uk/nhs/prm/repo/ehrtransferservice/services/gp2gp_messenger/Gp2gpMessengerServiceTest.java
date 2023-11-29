package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerContinueMessageRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerPositiveAcknowledgementRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class Gp2gpMessengerServiceTest {
    @Mock
    Gp2gpMessengerClient gp2gpMessengerClient;
    @Mock
    ParsedMessage parsedMessage;
    @Mock
    TransferStore transferStore;
    @Mock
    EhrCompleteEvent ehrCompleteEvent;
    @Mock
    Transfer ehrTransferData;
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
    void shouldThrowHttpExceptionWhenWeGotAnyStatusCodeButNot204ForEhrRequest() throws HttpException, URISyntaxException, IOException, InterruptedException {
        var incomingEvent = createIncomingEvent();

        doThrow(new HttpException()).when(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest(any(), any());

        Assertions.assertThrows(Exception.class, () -> gp2gpMessengerService.sendEhrRequest(incomingEvent));
    }

    @Test
    void shouldCallGp2GpMessengerClientForContinueRequest() throws HttpException, IOException, URISyntaxException, InterruptedException {
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(ehrTransferData.getSourceGP()).thenReturn("source-gp");
        gp2gpMessengerService.sendContinueMessage(parsedMessage, ehrTransferData);
        verify(gp2gpMessengerClient).sendContinueMessage(new Gp2gpMessengerContinueMessageRequestBody(conversationId,"source-gp",messageId));
    }

    @Test
    void shouldCallGp2gpMessengerForPositiveAcknowledgement() throws Exception {
        var conversationId = UUID.randomUUID();
        var messageId = UUID.randomUUID();
        var nhsNumber = "1234567890";

        when(ehrCompleteEvent.getConversationId()).thenReturn(conversationId);
        when(ehrCompleteEvent.getMessageId()).thenReturn(messageId);
        when(ehrTransferData.getSourceGP()).thenReturn("some-ods-code");
        when(ehrTransferData.getNhsNumber()).thenReturn(nhsNumber);

        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, ehrTransferData);

        var gp2gpMessengerPositiveAcknowledgementRequestBody =
                new Gp2gpMessengerPositiveAcknowledgementRequestBody("some-asid", "some-ods-code", conversationId.toString(), messageId.toString());
        verify(gp2gpMessengerClient).sendGp2gpMessengerPositiveAcknowledgement(nhsNumber, gp2gpMessengerPositiveAcknowledgementRequestBody);

    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp", "last-updated", "randomUUID");
    }

}