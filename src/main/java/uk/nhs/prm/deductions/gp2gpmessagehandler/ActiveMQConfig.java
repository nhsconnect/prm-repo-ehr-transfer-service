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

    @Value("${activemq.brokerUrl}")
    private String brokerUrl;

    @Value("${activemq.userName}")
    private String brokerUsername;

    @Value("${activemq.password}")
    private String brokerPassword;

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
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        activeMQConnectionFactory.setPassword(brokerPassword);
        activeMQConnectionFactory.setUserName(brokerUsername);
        return activeMQConnectionFactory;
    }
}