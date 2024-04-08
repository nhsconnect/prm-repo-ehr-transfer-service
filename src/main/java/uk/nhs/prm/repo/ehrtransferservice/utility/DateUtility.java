package uk.nhs.prm.repo.ehrtransferservice.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtility {
    private static final Logger LOGGER = LogManager.getLogger(DateUtility.class);

    private DateUtility() { }

    public static final String ZONE_ID = "Europe/London";

    /**
     * Gets an ISO timestamp in the format we require for DynamoDB.
     * @return The formatted String timestamp.
     */
    public static String getIsoTimestamp() {
        final String timestamp = ZonedDateTime.now(ZoneId.of(ZONE_ID))
            .truncatedTo(ChronoUnit.MINUTES)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        LOGGER.info("Generated ISO-8601 offset timestamp {}", timestamp);

        return timestamp;
    }
}