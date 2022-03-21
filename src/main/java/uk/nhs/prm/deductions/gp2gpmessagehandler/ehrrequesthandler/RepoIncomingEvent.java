package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import java.util.HashMap;

public class RepoIncomingEvent {
    private HashMap<String, Object> fields;

    public RepoIncomingEvent(HashMap<String, Object> fields) {
        this.fields = fields;
    }

    public String nhsNumber() {
        return fields.get("nhsNumber").toString();
    }

    public String sourceGP() {
        return fields.get("sourceGP").toString();
    }

    public String nemsMessageId() {
        return fields.get("nemsMessageId").toString();
    }

    public String destinationGP() {
        return fields.get("destinationGP").toString();
    }

}
