package uk.nhs.prm.repo.ehrtransferservice.models.sendEhrRequest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;

public class RegistrationRequestData {
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String type;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public UUID id;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public RegistrationRequestAttributes attributes;

    public RegistrationRequestData(String ehrRequestMessageId, UUID conversationId, String nhsNumber, String odsCode) {
        this.type = "registration-requests";
        this.id = conversationId;
        this.attributes = new RegistrationRequestAttributes(nhsNumber, odsCode, ehrRequestMessageId);
    }
}
