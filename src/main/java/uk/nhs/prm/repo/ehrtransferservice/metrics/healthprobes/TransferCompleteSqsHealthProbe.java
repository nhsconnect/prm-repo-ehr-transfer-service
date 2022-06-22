package uk.nhs.prm.repo.ehrtransferservice.metrics.healthprobes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;

@Slf4j
@Component
public class TransferCompleteSqsHealthProbe implements HealthProbe {
    private final AppConfig config;
    private final SqsClient sqsClient;


    @Autowired
    public TransferCompleteSqsHealthProbe(AppConfig config, SqsClient sqsClient) {
        this.config = config;
        this.sqsClient = sqsClient;
    }

    @Override
    public boolean isHealthy() {
        try {
            var queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName()).build()).queueUrl();
            sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(queueUrl).build());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SQS topic: " + config.transferCompleteQueueName(), exception);
            return false;
        }
    }

    private String queueName() {
        return config.transferCompleteQueueName();
    }
}
