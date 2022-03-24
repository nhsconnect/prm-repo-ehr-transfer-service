package uk.nhs.prm.repo.ehrtransferservice.gp2gpmessagemodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MhsJsonMessage {
    public String ebXML;
    public String payload;
}
