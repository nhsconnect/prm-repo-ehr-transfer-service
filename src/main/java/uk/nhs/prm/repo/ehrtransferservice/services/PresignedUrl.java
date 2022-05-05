package uk.nhs.prm.repo.ehrtransferservice.services;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

import java.io.IOException;
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

    public void uploadMessage(ParsedMessage parsedMessage) throws URISyntaxException, IOException, InterruptedException {
        var message = HttpRequest.BodyPublishers.ofString(parsedMessage.getRawMessage());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(presignedUrl.toURI())
                .PUT(message).build();
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected response from S3");
        }
    }
}
