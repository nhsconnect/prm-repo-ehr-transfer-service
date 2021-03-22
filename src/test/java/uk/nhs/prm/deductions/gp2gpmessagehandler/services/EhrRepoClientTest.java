package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class EhrRepoClientTest {

    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Test
    public void shouldFetchStorageUrlFromEhrRepo() throws MalformedURLException, URISyntaxException {
        String conversationId = "2592d6e6-3896-4c5c-a8b7-74216c9802f6";
        String messageId = "4e003f6f-6d03-4c98-8b08-54473b555f28";
        String presignedUrl = "https://fake-presigned-url";

        wireMock.stubFor(get(urlEqualTo("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(presignedUrl)
                        .withHeader("Content-Type", "application/json")));

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");
        PresignedUrl response = ehrRepoClient.fetchStorageUrl(UUID.fromString(conversationId), UUID.fromString(messageId));

        verify(getRequestedFor(urlMatching("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));

        assertThat(response.presignedUrl, Matchers.equalTo(new URL("https://fake-presigned-url")));
    }

    @Test
    public void shouldThrowErrorWhenCannotFetchStorageUrlFromEhrRepo() throws MalformedURLException {
        String conversationId = "2592d6e6-3896-4c5c-a8b7-74216c9802f6";
        String messageId = "4e003f6f-6d03-4c98-8b08-54473b555f28";

        wireMock.stubFor(get(urlEqualTo("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");
        Exception expected = assertThrows(RuntimeException.class, () ->
                ehrRepoClient.fetchStorageUrl(UUID.fromString(conversationId), UUID.fromString(messageId))
        );
        assertThat(expected, notNullValue());

        verify(getRequestedFor(urlMatching("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }
}
