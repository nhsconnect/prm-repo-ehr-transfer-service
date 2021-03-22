package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import de.mkammerer.wiremock.WireMockExtension;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.BytesMessage;
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

    private ActiveMQBytesMessage getActiveMQBytesMessage() throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(new byte[10]);
        bytesMessage.reset();
        return bytesMessage;
    }

    @Test
    void shouldUploadMessageToS3() throws JMSException, MalformedURLException, URISyntaxException {
        URL url = new URL(wireMock.baseUrl());
        BytesMessage message = getActiveMQBytesMessage();
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        presignedUrl.uploadMessage(message);

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(binaryEqualTo(new byte[(int) message.getBodyLength()])));
    }

    @Test
    void shouldThrowErrorWhenCannotUploadMessageToS3() throws JMSException, MalformedURLException, URISyntaxException {
        URL url = new URL(wireMock.baseUrl());
        BytesMessage message = getActiveMQBytesMessage();
        wireMock.stubFor(put(urlEqualTo("/")).willReturn(aResponse().withStatus(503)));

        PresignedUrl presignedUrl = new PresignedUrl(url);
        Exception expected = assertThrows(RuntimeException.class, () ->
                presignedUrl.uploadMessage(message)
        );
        assertThat(expected, notNullValue());

        verify(putRequestedFor(urlMatching("/"))
                .withRequestBody(binaryEqualTo(new byte[(int) message.getBodyLength()])));
    }
}
