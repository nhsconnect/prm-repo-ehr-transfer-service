package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Identifier {
    @XmlAttribute(name = "extension")
    public String extension;

    @XmlAttribute(name = "root")
    public String root;
}
