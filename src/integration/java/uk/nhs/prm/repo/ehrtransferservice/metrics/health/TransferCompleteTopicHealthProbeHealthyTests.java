package uk.nhs.prm.repo.ehrtransferservice.metrics.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import uk.nhs.prm.repo.ehrtransferservice.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.config.SnsClientSpringConfiguration;
import uk.nhs.prm.repo.ehrtransferservice.metrics.healthprobes.TransferCompleteSnsHealthProbe;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = {
		LocalStackAwsConfig.class, TransferCompleteSnsHealthProbe.class, AppConfig.class
})
class TransferCompleteTopicHealthProbeHealthyTests {

	@Autowired
	private TransferCompleteSnsHealthProbe probe;

	@Autowired
	private SnsClient snsClient;


	@Test
	void shouldReturnHealthyWhenTheProbeCanAccessTheQueue() {
		createTopic("test_transfer_complete_topic");
		assertThat(probe.isHealthy()).isEqualTo(true);
	}

	private void createTopic(String topicArn) {
		snsClient.createTopic(CreateTopicRequest.builder().name(topicArn).build());
	}

}
