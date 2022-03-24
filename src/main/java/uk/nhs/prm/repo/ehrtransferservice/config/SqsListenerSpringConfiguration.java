package uk.nhs.prm.repo.ehrtransferservice.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.ConversationIdGenerator;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.EhrRequestListener;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.EhrRequestService;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqsListenerSpringConfiguration {

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    private final Tracer tracer;
    private final ConversationIdGenerator conversationIdGenerator;
    private final EhrRequestService ehrRequestService;

    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.defaultClient();
    }

    @Bean
    public SQSConnection createConnection(AmazonSQSAsync amazonSQSAsync) throws JMSException {
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), amazonSQSAsync);
        return connectionFactory.createConnection();
    }

    @Bean
    public Session createListeners(SQSConnection connection) throws JMSException {
        Session session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
        log.info("repo incoming queue name : {}", repoIncomingQueueName);
        MessageConsumer consumer = session.createConsumer(session.createQueue(repoIncomingQueueName));

        consumer.setMessageListener(new EhrRequestListener(tracer,conversationIdGenerator,ehrRequestService));

        connection.start();

        return session;
    }
}
