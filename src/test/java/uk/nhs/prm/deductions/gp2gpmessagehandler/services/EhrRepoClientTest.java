package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
public class EhrRepoClientTest {

    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Test
    public void shouldFetchStorageUrlFromEhrRepo() throws MalformedURLException, URISyntaxException, HttpException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        String presignedUrl = "https://fake-presigned-url";

        wireMock.stubFor(get(urlEqualTo("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(presignedUrl)
                        .withHeader("Content-Type", "application/json")));

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");
        PresignedUrl response = ehrRepoClient.fetchStorageUrl(conversationId, messageId);

        verify(getRequestedFor(urlMatching("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));

        assertThat(response.presignedUrl, Matchers.equalTo(new URL("https://fake-presigned-url")));
    }

    @Test
    public void shouldThrowErrorWhenCannotFetchStorageUrlFromEhrRepo() throws MalformedURLException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        wireMock.stubFor(get(urlEqualTo("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");
        Exception expected = assertThrows(HttpException.class, () ->
                ehrRepoClient.fetchStorageUrl(conversationId, messageId)
        );
        assertThat(expected, notNullValue());

        verify(getRequestedFor(urlMatching("/messages/"+ conversationId + "/" + messageId))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

    @Test
    public void shouldThrowErrorWhenCannotStoreMessageInEhrRepo() throws MalformedURLException {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        wireMock.stubFor(post(urlEqualTo("/messages"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        when(mockParsedMessage.getNhsNumber()).thenReturn("0123456789");
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockParsedMessage.getAction()).thenReturn("RCMR_IN030000UK06");
        when(mockParsedMessage.getAttachmentMessageIds()).thenReturn(Collections.emptyList());

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");
        Exception expected = assertThrows(HttpException.class, () ->
                ehrRepoClient.confirmMessageStored(mockParsedMessage)
        );
        assertThat(expected, notNullValue());
    }

    @Test
    public void shouldConfirmMessageStoredInEhrRepo() throws MalformedURLException, URISyntaxException, HttpException {
        // Setup
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        String nhsNumber = "1234567890";
        String messageType = "ehrExtract";
        String interactionId = "RCMR_IN030000UK06";
        String requestBody = "{\"data\":{\"type\":\"messages\",\"id\":\"" + messageId + "\",\"attributes\":{\"conversationId\":\""+ conversationId +"\",\"messageType\":\""+ messageType +"\",\"nhsNumber\":\""+ nhsNumber +"\",\"attachmentMessageIds\":[\""+attachmentId+"\"]}}}";

        // Mock request
        wireMock.stubFor(post(urlEqualTo("/messages"))
                .withHeader("Authorization", equalTo("secret"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")));

        EhrRepoClient ehrRepoClient = new EhrRepoClient(wireMock.baseUrl(), "secret");

        // Create parsed message to store
        SOAPEnvelope envelope = getSoapEnvelope(conversationId, messageId, attachmentId, interactionId);
        EhrExtractMessageWrapper ehrExtractMessageWrapper = getMessageContent(nhsNumber);
        ParsedMessage parsedMessage = new ParsedMessage(envelope, ehrExtractMessageWrapper, null);

        // Store
        ehrRepoClient.confirmMessageStored(parsedMessage);

        verify(postRequestedFor(urlMatching("/messages"))
                .withRequestBody(equalToJson((requestBody)))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("secret")));
    }

    private EhrExtractMessageWrapper getMessageContent(String nhsNumber) {
        EhrExtractMessageWrapper ehrExtractMessageWrapper = new EhrExtractMessageWrapper();
        ehrExtractMessageWrapper.controlActEvent = new EhrExtractMessageWrapper.ControlActEvent();
        ehrExtractMessageWrapper.controlActEvent.subject = new EhrExtractMessageWrapper.ControlActEvent.Subject();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract = new EhrExtract();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget = new EhrExtract.RecordTarget();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient = new Patient();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient.id = new Identifier();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient.id.extension= nhsNumber;
        return ehrExtractMessageWrapper;
    }

    private SOAPEnvelope getSoapEnvelope(UUID conversationId, UUID messageId, UUID attachmentId, String interactionId) {
        SOAPEnvelope envelope = new SOAPEnvelope();
        Reference reference = new Reference();
        reference.href = "mid:"+attachmentId;
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(reference);
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.action = interactionId;
        envelope.header.messageHeader.conversationId = conversationId;
        envelope.header.messageHeader.messageData = new MessageData();
        envelope.header.messageHeader.messageData.messageId = messageId;
        return envelope;
    }
}
