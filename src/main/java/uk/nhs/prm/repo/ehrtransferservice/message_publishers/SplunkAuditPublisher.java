package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;

public class SplunkAuditPublisher {

    MessagePublisher messagePublisher;
    String auditTopicArn;

    public SplunkAuditPublisher(MessagePublisher messagePublisher, @Value("${aws.splunkUploaderTopicArn}") String auditTopicArn) {
        this.messagePublisher = messagePublisher;
        this.auditTopicArn = auditTopicArn;
    }

    public void sendMessage(SplunkAuditMessage message) {
        messagePublisher.sendMessage(auditTopicArn, new Gson().toJson(message));
    }
}
