package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;

import static org.assertj.core.api.Assertions.assertThat;

class StoreMessageResultTest {

    @Test
    public void ehrIsCompleteIfHealthRecordStatusIsComplete() {
        var result = new StoreMessageResult(new StoreMessageResponseBody("complete"));
        assertThat(result.isEhrComplete()).isTrue();
    }

    @Test
    public void ehrIsNotCompleteIfHealthRecordStatusIsAnythingOtherThanComplete() {
        var result = new StoreMessageResult(new StoreMessageResponseBody("not complete"));
        assertThat(result.isEhrComplete()).isFalse();
    }

    @Test
    public void ehrIsNotCompleteIfHealthRecordStatusIsMissing() {
        var result = new StoreMessageResult(new StoreMessageResponseBody(null));
        assertThat(result.isEhrComplete()).isFalse();
    }

    @Test
    public void ehrIsNotCompleteIfHealthRecordStatusIsEmpty() {
        var result = new StoreMessageResult(new StoreMessageResponseBody(""));
        assertThat(result.isEhrComplete()).isFalse();
    }
}