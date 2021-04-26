package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.mockito.Mockito.when;

@Tag("unit")
public class RepoToGPClientTest {
    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Mock
    ParsedMessage parsedMessage;

    private AutoCloseable closeable;
    UUID conversationId = UUID.randomUUID();
    String ehrRequestId = UUID.randomUUID().toString();
    String nhsNumber = "1234567890";
    String odsCode = "A12345";

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        conversationId = UUID.randomUUID();
        ehrRequestId = UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldCallRepoToGPToStartRegistrationRequest() throws IOException, HttpException, URISyntaxException, InterruptedException {
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getEhrRequestId()).thenReturn(ehrRequestId);
        when(parsedMessage.getNhsNumber()).thenReturn(nhsNumber);
        when(parsedMessage.getOdsCode()).thenReturn(odsCode);

        String requestBody = "{\"data\":{\"type\":\"registration-requests\",\"id\":\"" + conversationId + "\",\"attributes\":{\"ehrRequestId\":\"" + ehrRequestId + "\",\"odsCode\":\"" + odsCode + "\",\"nhsNumber\":\"" + nhsNumber + "\"}}}";

        wireMock.stubFor(post(urlEqualTo("/registration-requests"))
                .withHeader("Authorization", matching("secret"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withBody(requestBody)
                        .withHeader("Content-Type", "application/json")));

        RepoToGPClient repoToGPClient = new RepoToGPClient(wireMock.baseUrl(), "secret");
        repoToGPClient.sendEhrRequest(parsedMessage);

        verify(postRequestedFor(urlMatching("/registration-requests"))
                .withRequestBody(equalToJson((requestBody)))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }
}
