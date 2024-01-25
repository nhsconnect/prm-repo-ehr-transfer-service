package uk.nhs.prm.repo.ehrtransferservice.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
public class ActiveMQConfig {

    @Value("${activemq.openwireEndpoint1}")
    private String amqEndpoint1;

    @Value("${activemq.openwireEndpoint2}")
    private String amqEndpoint2;

    @Value("${activemq.userName}")
    private String brokerUsername;

    @Value("${activemq.password}")
    private String brokerPassword;

    @Value("${activemq.randomOption}")
    private String randomOption;

    @Bean
    public DefaultJmsListenerContainerFactory myFactory(ConnectionFactory connectionFactory,
                                                        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(failoverUrl());
        activeMQConnectionFactory.setPassword(brokerPassword);
        activeMQConnectionFactory.setUserName(brokerUsername);
        return activeMQConnectionFactory;
    }

    private String failoverUrl() {
        return String.format("failover:(%s,%s)%s", amqEndpoint1, amqEndpoint2, randomOption);
    }
}