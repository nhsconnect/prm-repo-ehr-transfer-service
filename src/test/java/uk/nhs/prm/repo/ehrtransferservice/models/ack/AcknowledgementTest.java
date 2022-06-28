package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class AcknowledgementTest {

    @Test
    public void shouldBeAPositiveAcknowledgementIfTypeIsAA() {
        var acknowledgementMessageWrapper = new AcknowledgementMessageWrapper();
        acknowledgementMessageWrapper.acknowledgement = new AcknowledgementMessageWrapper.Acknowledgement();
        acknowledgementMessageWrapper.acknowledgement.typeCode = "AA";

        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");

        assertThat(ack.isNegativeAcknowledgement()).isFalse();
    }
}
