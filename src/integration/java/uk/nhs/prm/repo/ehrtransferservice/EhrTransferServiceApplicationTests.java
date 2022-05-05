package uk.nhs.prm.repo.ehrtransferservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import javax.jms.BytesMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class EhrTransferServiceApplicationTests {
  @Autowired
  JmsTemplate jmsTemplate;

  @Value("${activemq.inboundQueue}")
  private String inboundQueue;


  @Value("${activemq.unhandledQueue}")
  private String unhandledQueue;


  private final TestDataLoader dataLoader = new TestDataLoader();
  private WireMockServer wireMock;

  @BeforeEach
  public void setUp() {
      wireMock = initializeWebServer();
  }

  @AfterEach
  public void tearDown() {
      wireMock.stop();
  }

  private WireMockServer initializeWebServer() {
      final WireMockServer wireMockServer = new WireMockServer(8080);
      wireMockServer.start();
      return wireMockServer;
  }

  @Test
  void shouldSendRegistrationRequestToRepoToGp() throws IOException, InterruptedException {
      String registrationRequestMessage = dataLoader.getDataAsString("RCMR_IN010000UK05");
      String conversationId = "17a757f2-f4d2-444e-a246-9cb77bef7f22";
      String ehrRequestId = "FFFB3C70-0BCC-4D9E-A441-7E9C41A897AA";
      String odsCode = "A91720";
      String nhsNumber = "9692842304";

      String requestBody = "{\"data\":{\"type\":\"registration-requests\",\"id\":\"" + conversationId + "\",\"attributes\":{\"ehrRequestId\":\"" + ehrRequestId + "\",\"odsCode\":\"" + odsCode + "\",\"nhsNumber\":\"" + nhsNumber + "\"}}}";
      wireMock.stubFor(post(urlMatching("/registration-requests")).willReturn(aResponse().withStatus(204)));

      jmsTemplate.send(inboundQueue, session -> {
          BytesMessage bytesMessage = session.createBytesMessage();
          bytesMessage.writeBytes(registrationRequestMessage.getBytes(StandardCharsets.UTF_8));
          return bytesMessage;
      });
      sleep(5000);
      verify(postRequestedFor(urlMatching("/registration-requests")).withRequestBody(equalToJson(requestBody)));
      jmsTemplate.setReceiveTimeout(1000);
      assertNull(jmsTemplate.receive(unhandledQueue));
  }
}
