package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RepoIncomingEventParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public RepoIncomingEvent parse(String repoIncomingMessage) throws JsonProcessingException {
        return new RepoIncomingEvent(mapper.readValue(repoIncomingMessage, new TypeReference<>() {
        }));
    }
}