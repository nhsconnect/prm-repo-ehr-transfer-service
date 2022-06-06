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
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrCompleteHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.listeners.EhrCompleteMessageListener;
import uk.nhs.prm.repo.ehrtransferservice.listeners.LargeEhrMessageListener;
import uk.nhs.prm.repo.ehrtransferservice.listeners.SmallEhrMessageListener;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.EhrCompleteParser;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventListener;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventParser;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingService;

import javax.jms.JMSException;
import javax.jms.Session;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqsListenerSpringConfiguration {

    private final Tracer tracer;
    private final RepoIncomingService repoIncomingService;
    private final RepoIncomingEventParser repoIncomingEventParser;
    private final SmallEhrMessageHandler smallEhrMessageHandler;
    private final LargeEhrMessageHandler largeEhrMessageHandler;
    private final EhrCompleteHandler ehrCompleteHandler;
    private final EhrCompleteParser ehrCompleteParser;
    private final Parser parser;
    private final S3PointerMessageHandler s3PointerMessageHandler;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

    @Value("${aws.largeEhrQueueName}")
    private String largeEhrQueueName;

    @Value("${aws.ehrCompleteQueueName}")
    private String ehrCompleteQueueName;

    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.defaultClient();
    }

    @Bean
    public SQSConnection createConnection(AmazonSQSAsync amazonSQSAsync) throws JMSException {
        var connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), amazonSQSAsync);
        return connectionFactory.createConnection();
    }

    @Bean
    public Session createRepoIncomingQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("repo incoming queue name : {}", repoIncomingQueueName);
        var incomingQueueConsumer = session.createConsumer(session.createQueue(repoIncomingQueueName));
        incomingQueueConsumer.setMessageListener(new RepoIncomingEventListener(tracer, repoIncomingService, repoIncomingEventParser));

        connection.start();

        return session;
    }

    @Bean
    public Session createSmallEhrQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("ehr small queue name : {}", smallEhrQueueName);
        var ehrSmallConsumer = session.createConsumer(session.createQueue(smallEhrQueueName));
        ehrSmallConsumer.setMessageListener(new SmallEhrMessageListener(tracer, parser, smallEhrMessageHandler));

        connection.start();

        return session;
    }

    @Bean
    public Session createLargeEhrQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("ehr small queue name : {}", largeEhrQueueName);
        var largeEhrConsumer = session.createConsumer(session.createQueue(largeEhrQueueName));
        largeEhrConsumer.setMessageListener(new LargeEhrMessageListener(tracer, s3PointerMessageHandler));

        connection.start();

        return session;
    }

    @Bean
    public Session createEhrCompleteQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("ehr complete queue name : {}", ehrCompleteQueueName);
        var ehrCompleteConsumer = session.createConsumer(session.createQueue(ehrCompleteQueueName));
        ehrCompleteConsumer.setMessageListener(new EhrCompleteMessageListener(tracer, ehrCompleteParser, ehrCompleteHandler));

        connection.start();

        return session;
    }

    private Session getSession(SQSConnection connection) throws JMSException {
        return connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
    }
}
