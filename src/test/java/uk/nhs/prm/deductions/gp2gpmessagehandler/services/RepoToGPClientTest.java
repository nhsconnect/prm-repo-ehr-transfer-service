package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;

@Tag("unit")
public class RepoToGPClientTest {
    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Test
    public void shouldCallRepoToGPToStartRegistrationRequest() throws IOException, HttpException, URISyntaxException, InterruptedException {
        UUID conversationId = UUID.randomUUID();
        String ehrRequestId = UUID.randomUUID().toString();
        String nhsNumber = "1234567890";
        String odsCode = "A12345";
        String requestBody = "{\"data\":{\"type\":\"registration-requests\",\"id\":\"" + conversationId + "\",\"attributes\":{\"ehrRequestId\":\"" + ehrRequestId + "\",\"odsCode\":\"" + odsCode + "\",\"nhsNumber\":\"" + nhsNumber + "\"}}}";

        wireMock.stubFor(post(urlEqualTo("/registration-requests"))
                .withHeader("Authorization", matching("secret"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withBody(requestBody)
                        .withHeader("Content-Type", "application/json")));

        RepoToGPClient repoToGPClient = new RepoToGPClient(wireMock.baseUrl(), "secret");
        repoToGPClient.sendEhrRequest(ehrRequestId, conversationId, nhsNumber, odsCode);

        verify(postRequestedFor(urlMatching("/registration-requests"))
                .withRequestBody(equalToJson((requestBody)))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }
}
