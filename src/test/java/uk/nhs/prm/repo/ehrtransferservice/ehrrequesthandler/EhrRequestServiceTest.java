package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EhrRequestServiceTest {

    @Mock
    TransferTrackerService transferTrackerService;
    @Mock
    RepoIncomingEventParser incomingEventParser;

    @InjectMocks
    EhrRequestService ehrRequestService;

    @Test
    void shouldParseIncomingEventMessage() throws JsonProcessingException {
        String incomingMessage = "{\"nhsNumber\":\"nhs-number\",\"sourceGP\":\"source-gp\",\"nemsMessageId\":\"nems-message-id\",\"destinationGP\":\"destination-GP\"}";
        ehrRequestService.processIncomingEvent(incomingMessage);

        verify(incomingEventParser).parse(incomingMessage);

    }

    @Test
    void shouldThrowAnExceptionWhenEncountersAnUnprocessableMessage() throws JsonProcessingException {
        String incomingMessage = "invalid";
        when(incomingEventParser.parse(incomingMessage)).thenThrow(JsonProcessingException.class);
        assertThrows(RuntimeException.class, () -> ehrRequestService.processIncomingEvent(incomingMessage));
    }

}