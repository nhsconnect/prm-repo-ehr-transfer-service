package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Data
public class TransferTrackerDbEntry{
    String nhsNumber;
    String sourceGP;
    String nemsMessageId;
    String destinationGP;
    String conversationId;
    String dateTime = getTimeNow();
    // add state
    private String getTimeNow(){
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
