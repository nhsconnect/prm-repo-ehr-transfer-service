package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class GPToRepoClientTest {

    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Test
    public void shouldCallApiContinueMessageWithValidEhrExtractId() throws URISyntaxException, MalformedURLException, HttpException {

        String conversationId = "08bf1791-4c56-4667-ad7c-64cf9e64ab1e";
        String ehrExtractMessageId = "ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717";
        wireMock.stubFor(patch(urlEqualTo("/deduction-requests/"+ conversationId +"/large-ehr-started"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/json")));
        GPToRepoClient gpToRepoClient = new GPToRepoClient(wireMock.baseUrl(), "secret");

        gpToRepoClient.sendContinueMessage(UUID.fromString(ehrExtractMessageId),UUID.fromString(conversationId));

        verify(patchRequestedFor(urlMatching("/deduction-requests/"+ conversationId +"/large-ehr-started"))
                .withRequestBody(equalToJson("{ \"ehrExtractMessageId\": \"ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717\"}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

    @Test
    public void shouldThrowErrorWhenUnexpectedResponseForContinueMessage() throws MalformedURLException {

        String conversationId = "08bf1791-4c56-4667-ad7c-64cf9e64ab1e";
        String ehrExtractMessageId = "ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717";
        wireMock.stubFor(patch(urlEqualTo("/deduction-requests/"+ conversationId +"/large-ehr-started"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));
        GPToRepoClient gpToRepoClient = new GPToRepoClient(wireMock.baseUrl(), "secret");
        Exception expected = assertThrows(HttpException.class, () ->
            gpToRepoClient.sendContinueMessage(UUID.fromString(ehrExtractMessageId), UUID.fromString(conversationId))
        );
        assertThat(expected, notNullValue());

        verify(patchRequestedFor(urlMatching("/deduction-requests/"+ conversationId +"/large-ehr-started"))
                .withRequestBody(equalToJson("{ \"ehrExtractMessageId\": \"ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717\"}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

    @Test
    public void shouldCallPdsUpdatedEndpointSuccessfully() throws MalformedURLException, URISyntaxException, HttpException {
        String conversationId = "08bf1791-4c56-4667-ad7c-64cf9e64ab1e";
        wireMock.stubFor(patch(urlEqualTo("/deduction-requests/"+ conversationId +"/pds-updated"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/json")));
        GPToRepoClient gpToRepoClient = new GPToRepoClient(wireMock.baseUrl(), "secret");

        gpToRepoClient.sendPdsUpdatedMessage(UUID.fromString(conversationId));

        verify(patchRequestedFor(urlMatching("/deduction-requests/"+ conversationId +"/pds-updated"))
                .withRequestBody(equalToJson("{}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

    @Test
    public void shouldThrowErrorWhenUnexpectedResponseForPdsUpdated() throws MalformedURLException {

        String conversationId = "08bf1791-4c56-4667-ad7c-64cf9e64ab1e";
        wireMock.stubFor(patch(urlEqualTo("/deduction-requests/"+ conversationId +"/pds-updated"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));
        GPToRepoClient gpToRepoClient = new GPToRepoClient(wireMock.baseUrl(), "secret");
        Exception expected = assertThrows(HttpException.class, () ->
                gpToRepoClient.sendPdsUpdatedMessage(UUID.fromString(conversationId))
        );
        assertThat(expected, notNullValue());

        verify(patchRequestedFor(urlMatching("/deduction-requests/"+ conversationId +"/pds-updated"))
                .withRequestBody(equalToJson("{}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

}
