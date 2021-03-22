package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PresignedUrl {
    public URL presignedUrl;

    public PresignedUrl (URL presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public void uploadMessage(BytesMessage bytesMessage) throws URISyntaxException, JMSException {
        byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
        HttpRequest.BodyPublisher message = HttpRequest.BodyPublishers.ofByteArray(contentAsBytes);

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
            throw new RuntimeException("Failed to store EHR in S3",e);
        }
    };
}
