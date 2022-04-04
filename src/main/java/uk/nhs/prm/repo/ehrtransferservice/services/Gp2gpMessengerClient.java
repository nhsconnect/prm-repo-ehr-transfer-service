package uk.nhs.prm.repo.ehrtransferservice.services;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class Gp2gpMessengerClient {

    private final URL gp2gpMessengerUrl;

    private final String gp2gpMessengerAuthKey;

    public Gp2gpMessengerClient(@Value("${gp2gpMessengerUrl}") String gp2gpMessengerUrl, @Value("${gp2gpMessengerAuthKey}") String gp2gpMessengerAuthKey) throws MalformedURLException {
        this.gp2gpMessengerUrl = new URL(gp2gpMessengerUrl);
        this.gp2gpMessengerAuthKey = gp2gpMessengerAuthKey;
    }

    public void sendGp2gpMessengerEhrRequest(String nhsNumber, Gp2gpMessengerEhrRequestBody body) throws IOException, URISyntaxException, InterruptedException, HttpException {
        String jsonPayloadString = new Gson().toJson(body);
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);
        String endpoint = "/health-record-requests/" + nhsNumber;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(gp2gpMessengerUrl, endpoint).toURI())
                .header("Authorization", gp2gpMessengerAuthKey)
                .header("Content-Type", "application/json")
                .POST(jsonPayload).build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new HttpException(String.format("Unexpected response from Gp2Gp messenger while posting a registration request: %d", response.statusCode()));
        }
    }

}

