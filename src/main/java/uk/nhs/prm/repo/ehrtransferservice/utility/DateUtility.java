package uk.nhs.prm.repo.ehrtransferservice.utility;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtility {
    private DateUtility() { }
    private static final String TIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ssZ";
    public static final String ZONE_ID = "Europe/London";

    /**
     * Gets an ISO timestamp in the format we require for DynamoDB.
     * @return The formatted String timestamp.
     */
    public static String getIsoTimestamp() {
        return ZonedDateTime.now(ZoneId.of(ZONE_ID))
            .truncatedTo(ChronoUnit.MINUTES)
            .format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }
}