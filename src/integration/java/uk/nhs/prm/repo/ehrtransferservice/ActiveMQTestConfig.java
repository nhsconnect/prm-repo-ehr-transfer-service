package uk.nhs.prm.repo.ehrtransferservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;


@Configuration
@EnableJms
public class ActiveMQTestConfig {
    public static final String QUEUE = "queue_one";

    private ActiveMQTestConfig() {

    }
}