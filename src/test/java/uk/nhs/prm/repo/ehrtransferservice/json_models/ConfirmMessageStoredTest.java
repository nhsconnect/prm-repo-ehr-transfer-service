package uk.nhs.prm.repo.ehrtransferservice.json_models;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageRequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConfirmMessageStoredTest {
    UUID messageId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    String nhsNumber = "1234567890";
    String messageType = "ehrExtract";
    List<UUID> attachmentMessageIds = new ArrayList<>();
    UUID attachmentMessageID = UUID.randomUUID();
    UUID secondAttachmentMessageID = UUID.randomUUID();

    @Test
    public void shouldSerializeToJson() {
        String expectedJson = "{\"data\":{\"type\":\"messages\",\"id\":\"" + messageId + "\",\"attributes\":{\"conversationId\":\"" + conversationId + "\",\"messageType\":\"" + messageType + "\",\"nhsNumber\":\"" + nhsNumber + "\",\"attachmentMessageIds\":[]}}}";
        String jsonText = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, attachmentMessageIds));
        assertThat(jsonText, equalTo(expectedJson));
    }

    @Test
    public void shouldSerializeToJsonWithAttachmentMessageIds() {
        attachmentMessageIds.add(attachmentMessageID);
        attachmentMessageIds.add(secondAttachmentMessageID);

        String expectedJson = "{\"data\":{\"type\":\"messages\",\"id\":\"" + messageId + "\",\"attributes\":{\"conversationId\":\"" + conversationId + "\",\"messageType\":\"" + messageType + "\",\"nhsNumber\":\"" + nhsNumber + "\",\"attachmentMessageIds\":[\"" + attachmentMessageID + "\",\"" + secondAttachmentMessageID + "\"]}}}";
        String jsonText = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, attachmentMessageIds));
        assertThat(jsonText, equalTo(expectedJson));
    }
}
