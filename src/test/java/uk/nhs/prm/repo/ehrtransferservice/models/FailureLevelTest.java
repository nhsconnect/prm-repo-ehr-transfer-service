package uk.nhs.prm.repo.ehrtransferservice.models;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailureLevelTest {

    @Test
    public void shouldParseErrorFromCode() {
        assertThat(FailureLevel.parse("ER")).isEqualTo(FailureLevel.ERROR);
    }

    @Test
    public void shouldParseWarningFromCode() {
        assertThat(FailureLevel.parse("WG")).isEqualTo(FailureLevel.WARNING);
    }

    @Test
    public void shouldParseInformationalOnlyFromCode() {
        assertThat(FailureLevel.parse("IF")).isEqualTo(FailureLevel.INFO);
    }

    @Test
    public void shouldParseAsUnknownIfNoCodePresent() {
        assertThat(FailureLevel.parse(null)).isEqualTo(FailureLevel.UNKNOWN);
    }

    @Test
    public void shouldParseAsUnknownIfCodeIsEmpty() {
        assertThat(FailureLevel.parse("")).isEqualTo(FailureLevel.UNKNOWN);
    }

    @Test
    public void shouldParseAsUnknownIfCodeIsUnexpectedOutOfSpecValueUsed() {
        assertThat(FailureLevel.parse("rather unexpected")).isEqualTo(FailureLevel.UNKNOWN);
    }
}