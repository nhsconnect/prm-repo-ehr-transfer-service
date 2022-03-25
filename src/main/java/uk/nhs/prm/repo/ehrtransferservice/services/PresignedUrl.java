package uk.nhs.prm.repo.ehrtransferservice.services;

import uk.nhs.prm.repo.ehrtransferservice.gp2gpmessagemodels.ParsedMessage;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PresignedUrl {
    public URL presignedUrl;

    public PresignedUrl(URL presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public void uploadMessage(ParsedMessage parsedMessage) throws URISyntaxException {
        String rawMessage = parsedMessage.getRawMessage();

        HttpRequest.BodyPublisher message = HttpRequest.BodyPublishers.ofString(rawMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(presignedUrl.toURI())
                .PUT(message).build();
        HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected response from S3");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store EHR in S3", e);
        }
    }
}
