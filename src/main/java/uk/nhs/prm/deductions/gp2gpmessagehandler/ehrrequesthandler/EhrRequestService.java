package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrRequestService {

    RepoIncomingEventParser incomingEventParser;

    public void processIncomingEvent(String incomingEvent) {
        parseMessage(incomingEvent);
    }

    private void parseMessage(String incomingEvent) {
        try {
            incomingEventParser.parse(incomingEvent);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse incoming event message");
            throw new RuntimeException(e);
        }
    }

}
