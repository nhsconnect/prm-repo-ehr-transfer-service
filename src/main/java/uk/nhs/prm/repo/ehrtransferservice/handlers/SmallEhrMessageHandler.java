package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmallEhrMessageHandler implements MessageHandler<ParsedMessage> {

    private final EhrRepoService ehrRepoService;

    private final TransferService transferService;

    @Override
    public void handleMessage(ParsedMessage parsedMessage) throws Exception {
        ehrRepoService.storeMessage(parsedMessage);
        log.info("The Small EHR has been stored in the ehr-repo-service successfully");
        // TODO PRMT-4524 make a call to transferService.markConversationAsComplete
        // TODO PRMT-4524 make a call to audit as we do in the LargeEhrCoreMessageHandler???

    }
}
