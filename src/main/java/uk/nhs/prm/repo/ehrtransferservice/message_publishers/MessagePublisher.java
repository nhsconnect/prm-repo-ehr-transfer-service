package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.sns.AmazonSNSExtendedClient;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessagePublisher {
    private final AmazonSNSExtendedClient snsClient;
    private final Tracer tracer;

    public void sendMessage(String topicArn, String message) {
        sendMessage(topicArn, message, null, null);
    }

    public void sendJsonMessage(String topicArn, Object message, String attributeKey, String attributeValue) {
        String jsonMessage = new Gson().toJson(message);
        sendMessage(topicArn, jsonMessage, attributeKey, attributeValue);
    }

    public void sendMessage(String topicArn, String message, String attributeKey, String attributeValue) {
        var messageAttributes = createMessageAttributes();
        if (attributeKey != null) {
            messageAttributes.put(attributeKey, getMessageAttributeValue(attributeValue));
        }

        PublishRequest request = new PublishRequest()
                .withMessage(message)
                .withMessageAttributes(messageAttributes)
                .withTopicArn(topicArn);

        var result = snsClient.publish(request);
        var topicAttributes = topicArn.split(":");
        log.info("PUBLISHED: message to {} topic. Published SNS message id: {}", topicAttributes[topicAttributes.length-1], result.getMessageId());
    }

    private Map<String, MessageAttributeValue> createMessageAttributes() {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("traceId", getMessageAttributeValue(tracer.getTraceId()));
        return messageAttributes;
    }

    private MessageAttributeValue getMessageAttributeValue(String attributeValue) {
        return new MessageAttributeValue().withDataType("String").withStringValue(attributeValue);
    }
}
