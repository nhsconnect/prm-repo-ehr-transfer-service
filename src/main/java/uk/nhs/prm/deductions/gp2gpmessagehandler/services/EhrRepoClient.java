package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels.confirmmessagestored.StoreMessageRequestBody;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@Service
public class EhrRepoClient {

    private final URL ehrRepoUrl;
    private final String ehrRepoAuthKey;

    public EhrRepoClient(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey) throws MalformedURLException {
        this.ehrRepoUrl = new URL(ehrRepoUrl);
        this.ehrRepoAuthKey = ehrRepoAuthKey;
    }

    public PresignedUrl fetchStorageUrl(UUID conversationId, UUID messageId) throws MalformedURLException, URISyntaxException, HttpException {
        String endpoint = "/messages/"+ conversationId + "/" + messageId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .GET().build();

        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected response from EHR Repo");
            }

            URL url = new URL(response.body());
            return new PresignedUrl(url);
        } catch (Exception e) {
            throw new HttpException("Failed to send a request to EHR Repo",e);
        }
    }

    public void confirmMessageStored(ParsedMessage parsedMessage) throws MalformedURLException, URISyntaxException, HttpException {
        String endpoint = "/messages";
        UUID conversationId = parsedMessage.getConversationId();
        UUID messageId = parsedMessage.getMessageId();
        String messageType = parsedMessage.getAction().equals("RCMR_IN030000UK06") ? "ehrExtract" : "attachment";
        String nhsNumber = parsedMessage.getNhsNumber();
        List<UUID> attachmentMessageIds = parsedMessage.getAttachmentMessageIds();

        String jsonPayloadString = new Gson().toJson(new StoreMessageRequestBody(messageId, conversationId, nhsNumber, messageType, attachmentMessageIds));
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(ehrRepoUrl, endpoint).toURI())
                .header("Authorization", ehrRepoAuthKey)
                .header("Content-Type", "application/json")
                .POST(jsonPayload).build();

        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new HttpException(String.format("Unexpected response from EHR Repo: %d", response.statusCode()));
            }

        } catch (Exception e) {
            throw new HttpException("Failed to send a request to EHR Repo",e);
        }
    }
}
