package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrCompleteHandler {
    private final Gp2gpMessengerService gp2gpMessengerService;

    public void handleMessage(EhrCompleteEvent ehrCompleteEvent) {
        log.info("In EHR complete handler");
        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent);
    }
}
