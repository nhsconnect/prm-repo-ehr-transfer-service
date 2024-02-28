package uk.nhs.prm.repo.ehrtransferservice.validator;

import uk.nhs.prm.repo.ehrtransferservice.database.TransferState;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ValidationUtility {
    private ValidationUtility() { }

    private static final String NHS_NUMBER_REGEX = "\\b[1-9]\\d{2}\\d{3}\\d{4}\\b";

    private static final String ODS_CODE_REGEX = "^[A-Z][0-9]{4,5}[A-Z]?$";

    private static final List<String> STATES;

    static {
        STATES = Arrays.stream(TransferState.class.getDeclaredFields())
            .map(Field::getName)
            .toList();
    }

    /**
     * Checks if a valid NHS number is present.
     * @param nhsNumber The NHS number to be validated.
     * @return true if a valid NHS number is present, otherwise false.
     */
    public static boolean isValidNhsNumber(String nhsNumber) {
        return nhsNumber.matches(NHS_NUMBER_REGEX);
    }

    /**
     * Checks if a valid ODS code is present.
     * @param odsCode The ODS code to be validated.
     * @return true if the ODS code is valid, otherwise false.
     */
    public static boolean isValidOdsCode(String odsCode) {
        return odsCode.matches(ODS_CODE_REGEX);
    }

    /**
     * Checks if a valid state is present.
     * @param state The state to be validated.
     * @return true if the state is valid, otherwise false.
     */
    public static boolean isValidState(String state) {
        return STATES.contains(state);
    }
}