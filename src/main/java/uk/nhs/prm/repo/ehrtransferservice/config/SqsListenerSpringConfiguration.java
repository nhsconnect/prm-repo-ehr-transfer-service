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
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.ConversationIdStore;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventListener;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingService;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventParser;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqsListenerSpringConfiguration {

    private final Tracer tracer;
    private final ConversationIdStore conversationIdStore;
    private final RepoIncomingService repoIncomingService;
    private final RepoIncomingEventParser parser;
    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

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

        consumer.setMessageListener(new RepoIncomingEventListener(tracer, conversationIdStore, repoIncomingService, parser));

        connection.start();

        return session;
    }
}
