package uk.nhs.prm.repo.ehrtransferservice.json_models.sendEhrRequest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class RegistrationRequestAttributes {
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String ehrRequestId;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String odsCode;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public String nhsNumber;

    public RegistrationRequestAttributes(String nhsNumber, String odsCode, String ehrRequestMessageId) {
        this.odsCode = odsCode;
        this.nhsNumber = nhsNumber;
        this.ehrRequestId = ehrRequestMessageId;
    }
}
