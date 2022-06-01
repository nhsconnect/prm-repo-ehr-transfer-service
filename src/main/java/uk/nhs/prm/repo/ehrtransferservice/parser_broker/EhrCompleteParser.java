package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;

@Component
@Slf4j
public class EhrCompleteParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public EhrCompleteEvent parse(String ehrCompleteMessage) {
        try {
            log.info("Trying to parse EHR Complete event");
            return mapper.readValue(ehrCompleteMessage, EhrCompleteEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse EHR Complete message");
            throw new RuntimeException(e);
        }
    }
}
