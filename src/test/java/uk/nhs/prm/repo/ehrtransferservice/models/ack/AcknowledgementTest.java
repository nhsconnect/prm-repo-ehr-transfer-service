package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class AcknowledgementTest {
    private AcknowledgementMessageWrapper acknowledgementMessageWrapper;

    @BeforeEach
    void setUp() {
        acknowledgementMessageWrapper = new AcknowledgementMessageWrapper();
        acknowledgementMessageWrapper.acknowledgement = new AcknowledgementMessageWrapper.Acknowledgement();
    }

    @Test
    public void shouldBeAPositiveAcknowledgementIfTypeIsAA() {
        acknowledgementMessageWrapper.acknowledgement.typeCode = "AA";
        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");
        assertThat(ack.isNegativeAcknowledgement()).isFalse();
    }

    @Test
    public void shouldBeANegativeAcknowledgementIfTypeIsAE() {
        acknowledgementMessageWrapper.acknowledgement.typeCode = "AE";
        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");
        assertThat(ack.isNegativeAcknowledgement()).isTrue();
    }

    @Test
    public void shouldBeANegativeAcknowledgementIfTypeIsAR() {
        acknowledgementMessageWrapper.acknowledgement.typeCode = "AR";
        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");
        assertThat(ack.isNegativeAcknowledgement()).isTrue();
    }

    @Test
    public void shouldBeANegativeAcknowledgementIfTypeIsUnknown() {
        acknowledgementMessageWrapper.acknowledgement.typeCode = "something that should never be set";
        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");
        assertThat(ack.isNegativeAcknowledgement()).isTrue();
    }
}
