package uk.nhs.prm.repo.ehrtransferservice.metrics.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient;
import uk.nhs.prm.repo.ehrtransferservice.metrics.healthprobes.TransferCompleteSnsHealthProbe;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class TransferCompleteHealthProbeHealthyTests {

    @Autowired
    private TransferCompleteSnsHealthProbe transferCompleteSnsHealthProbe;

    @Test
    void shouldReturnHealthyWhenTheProbeCanAccessTheQueue() {
        assertThat(transferCompleteSnsHealthProbe.isHealthy()).isEqualTo(true);
    }
}
