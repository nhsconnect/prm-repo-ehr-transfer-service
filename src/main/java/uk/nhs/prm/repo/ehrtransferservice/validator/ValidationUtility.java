package uk.nhs.prm.repo.ehrtransferservice.validator;

import uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ValidationUtility {
    private ValidationUtility() { }

    private static final String NHS_NUMBER_REGEX = "\\b\\d{3}\\d{3}\\d{4}\\b";

    private static final String ODS_CODE_REGEX = "(U\\d{5}|[AC]\\d{3}|V\\d{4}|[A-Za-z0-9]{5}\\d{3})$";

    private static final List<String> STATUSES;

    static {
        STATUSES = Arrays.stream(TransferStatus.class.getDeclaredFields())
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
     * Checks if a valid status is present.
     * @param status The status to be validated.
     * @return true if the status is valid, otherwise false.
     */
    public static boolean isValidStatus(String status) {
        return STATUSES.contains(status);
    }
}