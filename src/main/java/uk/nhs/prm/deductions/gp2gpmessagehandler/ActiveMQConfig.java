package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

import static javax.jms.Session.CLIENT_ACKNOWLEDGE;

@Configuration
public class ActiveMQConfig {

    @Value("${activemq.amqEndpoint1}")
    private String amqEndpoint1;

    @Value("${activemq.amqEndpoint2}")
    private String amqEndpoint2;

    @Value("${activemq.userName}")
    private String brokerUsername;

    @Value("${activemq.password}")
    private String brokerPassword;

    @Value("${activemq.randomOption}")
    private String randomOption;

    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(CLIENT_ACKNOWLEDGE);
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory(){
        ActiveMQConnectionFactory activeMQConnectionFactory  = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(failoverUrl());
        System.out.println("FAILOVER" + failoverUrl());
        activeMQConnectionFactory.setPassword(brokerPassword);
        activeMQConnectionFactory.setUserName(brokerUsername);
        return activeMQConnectionFactory;
    }

    private String failoverUrl() {
        return String.format("failover:(%s,%s)%s", amqEndpoint1, amqEndpoint2, randomOption);
    }
}