package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels.sendEhrRequest.RegistrationRequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class RepoToGPClient {
    private final URL repoToGPUrl;
    private final String repoToGPAuthKey;

    public RepoToGPClient(@Value("${repoToGPUrl}") String repoToGPUrl, @Value("${repoToGPAuthKey}") String repoToGPAuthKey) throws MalformedURLException {
        this.repoToGPUrl = new URL(repoToGPUrl);
        this.repoToGPAuthKey = repoToGPAuthKey;
    }

    public void sendEhrRequest(ParsedMessage parsedMessage) throws IOException, HttpException, InterruptedException, URISyntaxException {
        String endpoint = "/registration-requests";
        String ehrRequestMessageId = parsedMessage.getEhrRequestId();
        UUID conversationId = parsedMessage.getConversationId();
        String nhsNumber = parsedMessage.getNhsNumber();
        String odsCode = parsedMessage.getOdsCode();

        String jsonPayloadString = new Gson().toJson(new RegistrationRequestBody(ehrRequestMessageId, conversationId, nhsNumber, odsCode));
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(repoToGPUrl, endpoint).toURI())
                .header("Authorization", repoToGPAuthKey)
                .header("Content-Type", "application/json")
                .POST(jsonPayload).build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new HttpException(String.format("Unexpected response from Repo To GP while posting a registration request: %d", response.statusCode()));
        }
    }
}
