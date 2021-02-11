package uk.nhs.prm.deductions.gp2gpmessagehandler;

enum InteractionIds {
    EHR_REQUEST("RCMR_IN010000UK05"),
    EHR_REQUEST_COMPLETED("RCMR_IN030000UK06"),
    PDS_GENERAL_UPDATE_REQUEST_ACCEPTED("PRPA_IN000202UK01");

    private final String interactionId;

    InteractionIds(String interactionId) {
        this.interactionId = interactionId;
    }

    public String getInteractionId() {
        return interactionId;
    }
}

