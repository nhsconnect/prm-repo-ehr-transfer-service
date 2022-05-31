package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerPositiveAcknowledgementRequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
@Slf4j
public class Gp2gpMessengerClient {

    private final URL gp2gpMessengerUrl;

    private final String gp2gpMessengerAuthKey;

    private final Tracer tracer;

    public Gp2gpMessengerClient(@Value("${gp2gpMessengerUrl}") String gp2gpMessengerUrl, @Value("${gp2gpMessengerAuthKey}") String gp2gpMessengerAuthKey, Tracer tracer) throws MalformedURLException {
        this.gp2gpMessengerUrl = new URL(gp2gpMessengerUrl);
        this.gp2gpMessengerAuthKey = gp2gpMessengerAuthKey;
        this.tracer = tracer;
    }

    public void sendGp2gpMessengerEhrRequest(String nhsNumber, Gp2gpMessengerEhrRequestBody body) throws URISyntaxException, IOException, HttpException, InterruptedException {
        String jsonPayloadString = new Gson().toJson(body);
        HttpRequest.BodyPublisher jsonPayload = HttpRequest.BodyPublishers.ofString(jsonPayloadString);
        String endpoint = "/health-record-requests/" + nhsNumber;

        HttpRequest request = buildEhrRequest(jsonPayload, endpoint);
        HttpResponse<String> response = makeEhrRequest(request);

        if (response.statusCode() != 204) {
            throw new HttpException(String.format("Unexpected response from GP2GP messenger while posting an EHR request: %d", response.statusCode()));
        }
    }

    public void sendGp2gpMessengerPositiveAcknowledgement(String nhsNumber, Gp2gpMessengerPositiveAcknowledgementRequestBody gp2gpMessengerPositiveAcknowledgementRequestBody) {
    }

    private HttpRequest buildEhrRequest(HttpRequest.BodyPublisher jsonPayload, String endpoint) throws URISyntaxException, MalformedURLException {
        try {
            return HttpRequest.newBuilder()
                    .uri(new URL(gp2gpMessengerUrl, endpoint).toURI())
                    .header("Authorization", gp2gpMessengerAuthKey)
                    .header("Content-Type", "application/json")
                    .header("traceId", tracer.getTraceId())
                    .POST(jsonPayload).build();
        } catch (URISyntaxException | MalformedURLException e) {
            log.error("Error caught during building ehr-request");
            throw e;
        }
    }

    private HttpResponse<String> makeEhrRequest(HttpRequest request) throws IOException, InterruptedException {
        try {
            return HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Error caught during ehr-request");
            throw e;
        }
    }

    public void sendContinueMessage(UUID conversationId, UUID messageId, String odsCode) {
    }
}

