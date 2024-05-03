package uk.nhs.prm.repo.ehrtransferservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
public class RetryConfiguration
{ }