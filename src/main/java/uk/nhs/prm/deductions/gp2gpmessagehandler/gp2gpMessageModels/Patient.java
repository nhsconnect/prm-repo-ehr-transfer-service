package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {
    @JacksonXmlProperty(localName = "id")
    public Identifier id;

    public String getNhsNumber() {
        return this.id.extension;
    }
}
