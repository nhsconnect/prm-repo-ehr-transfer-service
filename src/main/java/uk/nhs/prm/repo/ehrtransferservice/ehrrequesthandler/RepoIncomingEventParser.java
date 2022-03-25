package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RepoIncomingEventParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public RepoIncomingEvent parse(String repoIncomingMessage) {
        try {
            return mapper.readValue(repoIncomingMessage, RepoIncomingEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse incoming event message");
            throw new RuntimeException(e);
        }
    }


}