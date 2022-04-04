package uk.nhs.prm.repo.ehrtransferservice.services;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockitoAnnotations;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Tag("unit")
public class Gp2gpMessengerClientTest {

    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldCallGP2GpMessengerEHRRequest() throws IOException, URISyntaxException, InterruptedException, HttpException {

        Gp2gpMessengerEhrRequestBody requestBody = new Gp2gpMessengerEhrRequestBody("","","","");

        String jsonPayloadString = new Gson().toJson(requestBody);

        wireMock.stubFor(post(urlEqualTo("/health-record-requests/1234567890"))
                .withHeader("Authorization", matching("secret"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withBody(jsonPayloadString)
                        .withHeader("Content-Type", "application/json")));

        Gp2gpMessengerClient gp2gpMessengerClient = new Gp2gpMessengerClient(wireMock.baseUrl(), "secret");
        gp2gpMessengerClient.sendGp2gpMessengerEhrRequest("1234567890", requestBody);

        verify(postRequestedFor(urlMatching("/health-record-requests/1234567890"))
                .withRequestBody(equalToJson((jsonPayloadString)))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));

    }

}
