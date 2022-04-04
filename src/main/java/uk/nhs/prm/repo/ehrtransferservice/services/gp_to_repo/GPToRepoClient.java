package uk.nhs.prm.repo.ehrtransferservice.services.gp_to_repo;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrExtractMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.HttpException;

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

    public GPToRepoClient(@Value("${gpToRepoUrl}") String gpToRepoUrl, @Value("${gpToRepoAuthKey}") String gpToRepoAuthKey) throws MalformedURLException {
        this.gpToRepoUrl = new URL(gpToRepoUrl);
        this.gpToRepoAuthKey = gpToRepoAuthKey;
    }

    public void sendContinueMessage(UUID messageId, UUID conversationId) throws HttpException {
        try {
            String jsonPayloadString = new Gson().toJson(new EhrExtractMessage(messageId));
            String endpoint = "/deduction-requests/" + conversationId + "/large-ehr-started";
            HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(gpToRepoUrl, endpoint).toURI())
                    .method("PATCH", jsonPayload)
                    .header("Authorization", gpToRepoAuthKey)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                throw new HttpException("Unexpected response from gp-to-repo when sending continue message");
            }
        } catch (Exception e) {
            throw new HttpException("Failed to send a request to gp-to-repo to send continue message", e);
        }
    }

    public void sendPdsUpdatedMessage(UUID conversationId) throws MalformedURLException, URISyntaxException, HttpException {
        String endpoint = "/deduction-requests/" + conversationId + "/pds-updated";
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString("{}");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(gpToRepoUrl, endpoint).toURI())
                .method("PATCH", jsonPayload)
                .header("Authorization", gpToRepoAuthKey)
                .header("Content-Type", "application/json")
                .build();

        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                throw new HttpException("Unexpected response from gp-to-repo when sending pds updated message: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new HttpException("Failed to send a request to gp-to-repo to send pds updated message", e);
        }
    }

    public void notifySmallEhrExtractArrived(UUID messageId, UUID conversationId) throws HttpException {
        try {
            String endpoint = "/deduction-requests/" + conversationId + "/ehr-message-received";
            String jsonPayloadString = new Gson().toJson(new EhrExtractMessage(messageId));
            HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(gpToRepoUrl, endpoint).toURI())
                    .method("PATCH", jsonPayload)
                    .header("Authorization", gpToRepoAuthKey)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                throw new HttpException("Unexpected response from gp-to-repo when sending the small EHR extract received notification");
            }
        } catch (Exception e) {
            throw new HttpException("Failed to send a request to gp-to-repo to send the small EHR extract received notification", e);
        }
    }
}
