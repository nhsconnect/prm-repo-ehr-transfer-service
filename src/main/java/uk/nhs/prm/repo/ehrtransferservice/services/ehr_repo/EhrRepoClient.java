package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.DuplicateMessageException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class EhrRepoClient {
    private final URL ehrRepoUrl;
    private final String ehrRepoAuthKey;
    private final Tracer tracer;


    public EhrRepoClient(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey, Tracer tracer) throws MalformedURLException {
        this.ehrRepoUrl = new URL(ehrRepoUrl);
        this.ehrRepoAuthKey = ehrRepoAuthKey;
        this.tracer = tracer;
    }

    public PresignedUrl fetchStorageUrl(UUID conversationId, UUID messageId) throws Exception {
        String endpoint = "/messages/" + conversationId + "/" + messageId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .GET().build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());


        if (response.statusCode() == 409) {
            throw new DuplicateMessageException("Tried to store and already existing message in EHR Repo.");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected response from EHR Repo when retrieving presigned URL");
        }

        URL url = new URL(response.body());
        return new PresignedUrl(url);
    }

    public StoreMessageResponseBody confirmMessageStored(ParsedMessage parsedMessage) throws Exception {
        var endpoint = "/messages";
        var conversationId = parsedMessage.getConversationId();
        var messageId = parsedMessage.getMessageId();
        var messageType = parsedMessage.getInteractionId().equals("RCMR_IN030000UK06") ? "ehrExtract" : "attachment";
        var nhsNumber = parsedMessage.getNhsNumber();
        var attachmentMessageIds = parsedMessage.getAttachmentMessageIds();

        var jsonPayloadString = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, attachmentMessageIds));
        var jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .header("traceId", tracer.getTraceId())
                .POST(jsonPayload).build();

        var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());


        if (response.statusCode() != 201) {
            throw new HttpException(String.format("Unexpected response from EHR while checking if a message was stored: %d", response.statusCode()));
        }
        return parseResponse(response);
    }

    private static StoreMessageResponseBody parseResponse(HttpResponse<String> response) throws JsonProcessingException {
        return new ObjectMapper().readValue(response.body(), StoreMessageResponseBody.class);
    }
}
