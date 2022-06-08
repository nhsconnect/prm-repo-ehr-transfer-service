package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtract {
    @JacksonXmlProperty(localName = "recordTarget")
    public RecordTarget recordTarget;

    @JacksonXmlProperty(localName = "author")
    public Author author;

    @JacksonXmlProperty(localName = "destination")
    public Destination destination;

    public Patient getPatient() {
        return this.recordTarget.patient;
    }

    public String getSourceGp() {
        return this.author.requestingPractice.agentOrganizationSDS.id.extension;
    }

    public String getDestinationGp() {
        return this.destination.requestingPractice.agentOrganizationSDS.id.extension;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordTarget {
        @JacksonXmlProperty(localName = "patient")
        public Patient patient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        @JacksonXmlProperty(localName = "AgentOrgSDS")
        public RequestingPractice requestingPractice;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Destination {
        @JacksonXmlProperty(localName = "AgentOrgSDS")
        public RequestingPractice requestingPractice;
    }
}
