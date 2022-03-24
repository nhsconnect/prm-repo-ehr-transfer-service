package uk.nhs.prm.repo.ehrtransferservice.jsonmodels.sendEhrRequest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;

public class RegistrationRequestBody {
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public RegistrationRequestData data;

    public RegistrationRequestBody(String ehrRequestMessageId, UUID conversationId, String nhsNumber, String odsCode) {
        this.data = new RegistrationRequestData(ehrRequestMessageId, conversationId, nhsNumber, odsCode);
    }
}
