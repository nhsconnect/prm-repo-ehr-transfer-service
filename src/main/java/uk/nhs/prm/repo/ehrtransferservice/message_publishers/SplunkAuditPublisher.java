package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;

public class SplunkAuditPublisher {

    MessagePublisher messagePublisher;
    String auditTopicArn;

    public SplunkAuditPublisher(MessagePublisher messagePublisher, @Value("${aws.splunkUploaderTopicArn}") String auditTopicArn) {
        this.messagePublisher = messagePublisher;
        this.auditTopicArn = auditTopicArn;
    }

    public void sendMessage(String message) {
        messagePublisher.sendMessage(auditTopicArn,message);
    }
}
