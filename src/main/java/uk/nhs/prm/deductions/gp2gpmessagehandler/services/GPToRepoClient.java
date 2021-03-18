package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels.EhrExtractMessage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class GPToRepoClient {

    private final URL gpToRepoUrl;
    private final String gpToRepoAuthKey;

    public GPToRepoClient (@Value("${gpToRepoUrl}") String gpToRepoUrl, @Value("${gpToRepoAuthKey}") String gpToRepoAuthKey) throws MalformedURLException {
        this.gpToRepoUrl = new URL(gpToRepoUrl);
        this.gpToRepoAuthKey = gpToRepoAuthKey;
    }

    public void sendContinueMessage(UUID ehrExtractMessageId, UUID conversationId) throws MalformedURLException, URISyntaxException {
        String jsonPayloadString = new Gson().toJson(new EhrExtractMessage(ehrExtractMessageId));
        String endpoint = "/deduction-requests/"+ conversationId + "/large-ehr-started";
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(gpToRepoUrl, endpoint).toURI())
                .method("PATCH", jsonPayload)
                .header("Authorization", gpToRepoAuthKey)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send a request to gp-to-repo",e);
        }
        if (response.statusCode() != 204) {
            throw new RuntimeException("Unexpected response from gp-to-repo");
        }
    }
}
