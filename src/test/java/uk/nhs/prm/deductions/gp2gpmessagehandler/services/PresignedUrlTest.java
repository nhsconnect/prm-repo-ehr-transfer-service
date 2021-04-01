package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.JMSException;
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

    private byte[] messageContent;

    public PresignedUrlTest() {
        messageContent = new byte[10];
        messageContent[0] = (byte) 234;
    }

    private ActiveMQBytesMessage getMessageAsBytes(byte[] messageContent) throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(messageContent);
        bytesMessage.reset();
        return bytesMessage;
    }

    @Test
    void shouldUploadMessageToS3() throws JMSException, MalformedURLException, URISyntaxException {
        URL url = new URL(wireMock.baseUrl());
        ActiveMQBytesMessage bytesMessage = getMessageAsBytes(messageContent);
        ParsedMessage parsedMessage = new ParsedMessage(null, null, bytesMessage);
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        presignedUrl.uploadMessage(parsedMessage);

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(binaryEqualTo(messageContent)));
    }

    @Test
    void shouldThrowErrorWhenCannotUploadMessageToS3() throws JMSException, MalformedURLException {
        URL url = new URL(wireMock.baseUrl());
        ActiveMQBytesMessage bytesMessage = getMessageAsBytes(messageContent);
        ParsedMessage parsedMessage = new ParsedMessage(null, null, bytesMessage);
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(503)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        Exception expected = assertThrows(RuntimeException.class, () ->
                presignedUrl.uploadMessage(parsedMessage)
        );
        assertThat(expected, notNullValue());

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(binaryEqualTo(messageContent)));
    }
}
