package uk.nhs.prm.repo.ehrtransferservice.validator;

import uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtility {
    private ValidationUtility() { }

    private static final Pattern NHS_NUMBER_REGEX = Pattern.compile("\\b\\d{3}\\d{3}\\d{4}\\b");

    private static final Pattern ODS_CODE_REGEX = Pattern.compile("(U\\d{5}|[AC]\\d{3}|V\\d{4}|[A-Za-z0-9]{5}\\d{3})$");

    private static final List<String> STATUSES;

    static {
        STATUSES = Arrays.stream(TransferStatus.class.getDeclaredFields())
            .map(Field::getName)
            .toList();
    }

    /**
     * Checks if a valid NHS number is present.
     * @param nhsNumber The NHS number to be validated.
     * @return true is a valid NHS number is present, otherwise false.
     */
    public static boolean isValidNhsNumber(String nhsNumber) {
        return false;
    }

    /**
     * Primary Care Networks (PCN): PCN ODS codes are 6-character codes, all starting with a ‘U’ followed by 5 numbers, for example, U12345.
     * Care Home and Domiciliary Care providers: These providers have at least 2 codes. An HQ code or “parent” code is 4 digits and normally starts with an A or C, e.g. A*** or C***. One or more site or “child” codes are 5 digits and normally start with V, e.g. V****.
     * Branch Surgery codes: These are allocated using the existing NHS PS GP Practice code for the parent Surgery, to which a further three numerical characters are added to define each branch e.g. 001, 002 etc.
     * @param odsCode The ODS code needing to be validated.
     * @return true if the ODS code is valid, otherwise false.
     */
    public static boolean isValidOdsCode(String odsCode) {
        return false;
    }

    /**
     * Checks if a valid status is present.
     * @param status The status to be validated.
     * @return true is the status is valid, otherwise false.
     */
    public static boolean isValidStatus(String status) {
        return STATUSES.contains(status);
    }
}