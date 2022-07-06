package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;

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
        var splunkAuditMessage = new SplunkAuditMessage("conversationId", "nemsMessageId", "status");
        splunkAuditPublisher.sendMessage(splunkAuditMessage);
        verify(messagePublisher).sendMessage(auditTopicArn, new Gson().toJson(splunkAuditMessage));
    }
}