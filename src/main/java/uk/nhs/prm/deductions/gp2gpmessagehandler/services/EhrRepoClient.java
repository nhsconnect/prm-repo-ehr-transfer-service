package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class EhrRepoClient {

    private final URL ehrRepoUrl;
    private final String ehrRepoAuthKey;

    public EhrRepoClient(@Value("${ehrRepoUrl}") String ehrRepoUrl, @Value("${ehrRepoAuthKey}") String ehrRepoAuthKey) throws MalformedURLException {
        this.ehrRepoUrl = new URL(ehrRepoUrl);
        this.ehrRepoAuthKey = ehrRepoAuthKey;
    }

    public PresignedUrl fetchStorageUrl(UUID conversationId, UUID messageId) throws MalformedURLException, URISyntaxException {
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
            throw new RuntimeException("Failed to send a request to EHR Repo",e);
        }
    }
}
