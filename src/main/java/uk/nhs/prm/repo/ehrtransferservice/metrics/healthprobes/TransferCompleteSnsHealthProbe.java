package uk.nhs.prm.repo.ehrtransferservice.metrics.healthprobes;

import com.amazonaws.services.sns.AmazonSNS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;

@Slf4j
@Component
public class TransferCompleteSnsHealthProbe implements HealthProbe {
    private final AppConfig config;
    private final AmazonSNS snsClient;

    @Autowired
    public TransferCompleteSnsHealthProbe(AppConfig config, AmazonSNS snsClient) {
        this.config = config;
        this.snsClient = snsClient;
    }

    @Override
    public boolean isHealthy() {
        try {
            snsClient.getTopicAttributes(config.transferCompleteTopicArn());
            return true;
        } catch (RuntimeException exception) {
            log.info("Failed to query SNS topic: " + config.transferCompleteTopicArn(), exception);
            return false;
        }
    }
}
