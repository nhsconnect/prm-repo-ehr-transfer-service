package uk.nhs.prm.repo.ehrtransferservice.metrics.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.config.SqsClientSpringConfiguration;
import uk.nhs.prm.repo.ehrtransferservice.metrics.healthprobes.TransferCompleteSqsHealthProbe;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalStackAwsConfig.class)
class TransferCompleteQueueHealthProbeHealthyTests {

	@Autowired
	private TransferCompleteSqsHealthProbe probe;


	@Value("${aws.transferCompleteQueueName}")
	private String transferCompleteQueueName;

	@Test
	void shouldReturnHealthyWhenTheProbeCanAccessTheQueue() {
		assertThat(probe.isHealthy()).isEqualTo(true);
	}

}
