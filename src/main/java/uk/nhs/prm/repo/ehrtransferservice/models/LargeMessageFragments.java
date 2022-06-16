package uk.nhs.prm.repo.ehrtransferservice.models;

public class LargeMessageFragments extends LargeSqsMessage {

    private String nhsNumber;

    public LargeMessageFragments(LargeSqsMessage largeSqsMessage, String nhsNumber) {
        super(largeSqsMessage.getSoapEnvelope(), largeSqsMessage.getMessageContent(), largeSqsMessage.getRawMessage());
        this.nhsNumber = nhsNumber;

    }

    public void setNhsNumber(String nhsNumber){
        this.nhsNumber = nhsNumber;
    }

    @Override
    public String getNhsNumber() {
        return nhsNumber;
    }
}
