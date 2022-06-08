package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("unit")
public class EhrExtractMessageWrapperTest {
    private final String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<RCMR_IN030000UK06 xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:hl7-org:v3 ..SchemasRCMR_IN030000UK06.xsd\">\n" +
            "    <ControlActEvent classCode=\"CACT\" moodCode=\"EVN\">\n" +
            "        <subject typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
            "            <EhrExtract classCode=\"EXTRACT\" moodCode=\"EVN\">\n" +
            "                <recordTarget typeCode=\"RCT\">\n" +
            "                    <patient classCode=\"PAT\">\n" +
            "                        <id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9442964410\" />\n" +
            "                    </patient>" +
            "                </recordTarget>" +
            "                <author typeCode=\"AUT\">" +
            "                   <time value=\"20220608101110\"/>" +
            "                   <signatureCode code=\"S\"/>" +
            "                   <signatureText>X</signatureText>" +
            "                   <AgentOrgSDS classCode=\"AGNT\">" +
            "                       <agentOrganizationSDS classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
            "                          <id root=\"1.2.826.0.1285.0.1.10\" extension=\"N82668\"/>" +
            "                       </agentOrganizationSDS>" +
            "                   </AgentOrgSDS>" +
            "               </author>" +
            "              <destination typeCode=\"DST\">" +
            "                   <AgentOrgSDS classCode=\"AGNT\">" +
            "                       <agentOrganizationSDS classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
            "                           <id root=\"1.2.826.0.1285.0.1.10\" extension=\"B85002\"/>" +
            "                       </agentOrganizationSDS>" +
            "                   </AgentOrgSDS>" +
            "               </destination>" +
            "             </EhrExtract>" +
            "         </subject>" +
            "    </ControlActEvent>" +
            "</RCMR_IN030000UK06>";

    @Test
    public void shouldRetrieveNHSNumberFromEHRExtract() throws JsonProcessingException {
        var ehrExtractMessageWrapper = new XmlMapper().readValue(extract, EhrExtractMessageWrapper.class);
        var ehrExtract = ehrExtractMessageWrapper.getEhrExtract();
        var patient = ehrExtract.getPatient();
        assertThat(patient.getNhsNumber(), equalTo("9442964410"));
    }

    @Test
    public void shouldRetrieveSourceGpFromEHRExtract() throws JsonProcessingException {
        var ehrExtractMessageWrapper = new XmlMapper().readValue(extract, EhrExtractMessageWrapper.class);
        var ehrExtract = ehrExtractMessageWrapper.getEhrExtract();
        assertThat(ehrExtract.getSourceGp(), equalTo("N82668"));
    }

    @Test
    public void shouldRetrieveDestinationGpFromEHRExtract() throws JsonProcessingException {
        var ehrExtractMessageWrapper = new XmlMapper().readValue(extract, EhrExtractMessageWrapper.class);
        var ehrExtract = ehrExtractMessageWrapper.getEhrExtract();
        assertThat(ehrExtract.getDestinationGp(), equalTo("B85002"));
    }
}
