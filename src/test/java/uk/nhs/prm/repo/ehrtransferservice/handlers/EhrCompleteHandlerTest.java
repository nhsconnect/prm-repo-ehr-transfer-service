package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrCompleteHandlerTest {

    @Mock
    EhrCompleteEvent ehrCompleteEvent;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @InjectMocks
    EhrCompleteHandler ehrCompleteHandler;

    @Test
    public void shouldCallGp2gpMessengerServiceToSendPositiveAcknowledgement() {
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent);
    }
}