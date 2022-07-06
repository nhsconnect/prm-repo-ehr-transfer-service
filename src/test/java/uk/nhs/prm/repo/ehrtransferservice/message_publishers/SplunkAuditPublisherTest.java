package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SplunkAuditPublisherTest {
    @Mock
    MessagePublisher messagePublisher;

    String auditTopicArn = "audit-topic-arn";

    SplunkAuditPublisher splunkAuditPublisher;

    @BeforeEach
    void setup() {
        splunkAuditPublisher = new SplunkAuditPublisher(messagePublisher, auditTopicArn);
    }

    @Test
    void shouldInvokeCallToPublishMessageToTheTopicWhenSendMessageIsInvoked() {
        splunkAuditPublisher.sendMessage("A message");
        verify(messagePublisher).sendMessage(auditTopicArn, "A message");
    }

}