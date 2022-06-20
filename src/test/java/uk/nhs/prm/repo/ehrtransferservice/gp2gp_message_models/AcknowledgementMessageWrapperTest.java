package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class AcknowledgementMessageWrapperTest {

    @Test
    public void getErrorDisplayNameFromReasonShouldReturnItsCodeDisplayName() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = anEventCode();
        reason.justifyingDetectedIssueEvent.code.displayName = "bob";

        assertThat(reason.getErrorDisplayName()).isEqualTo("bob");
    }

    @Test
    public void getErrorDisplayNameFromReasonWithoutEventShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = null;

        assertThat(reason.getErrorDisplayName()).isNull();
    }

    @Test
    public void getErrorDisplayNameFromReasonWithAnEventWithoutACodeShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = null;

        assertThat(reason.getErrorDisplayName()).isNull();
    }

    @Test
    public void getCodeFromReasonShouldReturnTheCodeOfItsCode() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        var eventCode = anEventCode();
        eventCode.code = "BOOM";
        reason.justifyingDetectedIssueEvent.code = eventCode;

        assertThat(reason.getCode()).isEqualTo("BOOM");
    }

    @Test
    public void getCodeFromReasonWithoutEventShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = null;

        assertThat(reason.getCode()).isNull();
    }

    @Test
    public void getCodeFromReasonWithEventButNoEventCodeShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = null;

        assertThat(reason.getCode()).isNull();
    }

    @Test
    public void getCodeSystemFromReasonShouldReturnItWhenPresent() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        var eventCode = anEventCode();
        eventCode.codeSystem = "1.2.da.bomb";
        reason.justifyingDetectedIssueEvent.code = eventCode;

        assertThat(reason.getCodeSystem()).isEqualTo("1.2.da.bomb");
    }

    @Test
    public void getCodeSystemFromReasonWithoutEventCodeShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = null;

        assertThat(reason.getCodeSystem()).isNull();
    }

    @Test
    public void getQualifierFromReasonShouldReturnItWhenPresent() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = anEventCode();
        reason.justifyingDetectedIssueEvent.code.qualifier = aQualifier();
        reason.justifyingDetectedIssueEvent.code.qualifier.code = "ER";

        assertThat(reason.getQualifier()).isEqualTo("ER");
    }

    @Test
    public void getQualifierFromReasonWithoutQualifierShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = anEventCode();
        reason.justifyingDetectedIssueEvent.code.qualifier = null;

        assertThat(reason.getQualifier()).isNull();
    }

    @Test
    public void getQualifierFromReasonWithoutEventCodeShouldReturnNull() {
        var reason = new AcknowledgementMessageWrapper.ControlActEvent.Reason();
        reason.justifyingDetectedIssueEvent = anEvent();
        reason.justifyingDetectedIssueEvent.code = null;

        assertThat(reason.getQualifier()).isNull();
    }

    @Test
    public void reasonsShouldBeEmptyWhenThereIsNoControlActElementThen(){
        var wrapper = new AcknowledgementMessageWrapper();
        wrapper.controlActEvent = null;

        assertThat(wrapper.reasons()).isEmpty();
    }

    @Test
    public void acknowledgementDetailsShouldBeEmptyWhenThereIsNoAcknowledgementDetailsInAcknowledgement(){
        var wrapper = new AcknowledgementMessageWrapper();
        wrapper.acknowledgement = new AcknowledgementMessageWrapper.Acknowledgement();
        wrapper.acknowledgement.acknowledgementDetail = null;

        assertThat(wrapper.acknowledgementDetails()).isEmpty();
    }

    @Test
    public void acknowledgementDetailPropertiesReturnNullWhenCodeElementIsMissing(){
        var wrapper = new AcknowledgementMessageWrapper();
        wrapper.acknowledgement = new AcknowledgementMessageWrapper.Acknowledgement();

        var detail = new AcknowledgementMessageWrapper.Acknowledgement.AcknowledgementDetail();
        wrapper.acknowledgement.acknowledgementDetail = List.of(detail);

        detail.code = null;

        assertThat(detail.getDisplayName()).isNull();
        assertThat(detail.getCode()).isNull();
        assertThat(detail.getCodeSystem()).isNull();
    }

    private AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent.Code anEventCode() {
        return new AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent.Code();
    }

    private AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent anEvent() {
        return new AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent();
    }

    private AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent.Code.Qualifier aQualifier() {
        return new AcknowledgementMessageWrapper.ControlActEvent.Reason.JustifyingDetectedIssueEvent.Code.Qualifier();
    }

}