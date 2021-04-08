package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class PresignedUrlTest {

    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Test
    void shouldUploadMessageToS3() throws MalformedURLException, URISyntaxException {
        URL url = new URL(wireMock.baseUrl());
        String rawMessage = "test";
        ParsedMessage parsedMessage = new ParsedMessage(null, null, rawMessage);
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        presignedUrl.uploadMessage(parsedMessage);

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(equalTo(rawMessage)));
    }

    @Test
    void shouldThrowErrorWhenCannotUploadMessageToS3() throws MalformedURLException {
        URL url = new URL(wireMock.baseUrl());
        String rawMessage = "test";
        ParsedMessage parsedMessage = new ParsedMessage(null, null, rawMessage);
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(503)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        Exception expected = assertThrows(RuntimeException.class, () ->
                presignedUrl.uploadMessage(parsedMessage)
        );
        assertThat(expected, notNullValue());

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(equalTo(rawMessage)));
    }
}
