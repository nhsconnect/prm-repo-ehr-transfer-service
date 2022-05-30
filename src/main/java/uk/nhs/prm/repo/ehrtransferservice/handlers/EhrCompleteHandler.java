package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;

@Service
@Slf4j
public class EhrCompleteHandler {
    public void handleMessage(EhrCompleteEvent ehrCompleteEvent) {
        log.info("In EHR complete handler");
    }
}
