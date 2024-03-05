package uk.nhs.prm.repo.ehrtransferservice.services;

import com.amazonaws.util.Md5Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Getter
@AllArgsConstructor
public class PresignedUrl {
    private final URL url;

    public void uploadMessage(ParsedMessage parsedMessage) throws URISyntaxException, IOException, InterruptedException {
        final String messageBody = parsedMessage.getMessageBody();
        var message = HttpRequest.BodyPublishers.ofString(messageBody);
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(url.toURI())
            .PUT(message)
            .header("Content-MD5", Md5Utils.md5AsBase64(messageBody.getBytes()))
            .build();

        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Unexpected response from S3 with status code :"+ response.statusCode());
        }
    }
}