package uk.nhs.prm.repo.ehrtransferservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.nhs.prm.repo.ehrtransferservice.validator.ValidationUtility.isValidNhsNumber;
import static uk.nhs.prm.repo.ehrtransferservice.validator.ValidationUtility.isValidOdsCode;
import static uk.nhs.prm.repo.ehrtransferservice.validator.ValidationUtility.isValidState;

class ValidationUtilityTest {
    private static final String VALID_STATE = "EHR_TRANSFER_STARTED";
    private static final String INVALID_STATE = "EHR_DELETED_FROM_MESH_MAILBOX";

    @ParameterizedTest
    @ValueSource(strings = {"9798546215", "1475854125", "4751425474"})
    void given_validNhsNumber_when_isValidNhsNumberCalled_then_returnTrue(String nhsNumber) {
        // when
        final boolean result = isValidNhsNumber(nhsNumber);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0147585412", "0000000000", "-1475845512"})
    void given_invalidNhsNumber_when_isValidNhsNumberCalled_then_returnFalse(String nhsNumber) {
        // when
        final boolean result = isValidNhsNumber(nhsNumber);

        // then
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"M85019", "B85002", "N82668"})
    void given_validOdsCode_when_isValidOdsCodeCalled_then_returnTrue(String odsCode) {
        // when
        final boolean result = isValidOdsCode(odsCode);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"B741", "B0.123", "0xFFFFF"})
    void given_invalidOdsCode_when_isValidOdsCodeCalled_then_returnFalse(String odsCode) {
        // when
        final boolean result = isValidOdsCode(odsCode);

        // then
        assertFalse(result);
    }

    @Test
    void given_validStatus_when_isValidStatusCalled_then_returnTrue() {
        // when
        final boolean result = isValidState(VALID_STATE);

        // then
        assertTrue(result);
    }

    @Test
    void given_invalidStatus_when_isValidStatusCalled_then_returnFalse() {
        // when
        final boolean result = isValidState(INVALID_STATE);

        // then
        assertFalse(result);
    }
}