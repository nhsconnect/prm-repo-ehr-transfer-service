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
import uk.nhs.prm.repo.ehrtransferservice.handlers.*;
import uk.nhs.prm.repo.ehrtransferservice.listeners.*;
import uk.nhs.prm.repo.ehrtransferservice.parsers.EhrCompleteParser;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3ExtendedMessageFetcher;
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
    private final LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;
    private final EhrCompleteHandler ehrCompleteHandler;
    private final EhrCompleteParser ehrCompleteParser;
    private final Parser parser;
    private final S3ExtendedMessageFetcher s3ExtendedMessageFetcher;
    private final LargeMessageFragmentHandler largeMessageFragmentHandler;
    private final NegativeAcknowledgementHandler negativeAcknowledgementHandler;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

    @Value("${aws.largeEhrQueueName}")
    private String largeEhrQueueName;

    @Value("${aws.largeMessageFragmentsQueueName}")
    private String largeMessageFragmentsQueueName;

    @Value("${aws.ehrCompleteQueueName}")
    private String ehrCompleteQueueName;

    @Value("${aws.nackQueueName}")
    private String negativeAckQueueName;

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
        ehrSmallConsumer.setMessageListener(new SmallEhrMessageListener(tracer, parser, smallEhrMessageHandler, s3ExtendedMessageFetcher));

        connection.start();

        return session;
    }

    @Bean
    public Session createLargeEhrQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("ehr small queue name : {}", largeEhrQueueName);
        var largeEhrConsumer = session.createConsumer(session.createQueue(largeEhrQueueName));
        largeEhrConsumer.setMessageListener(new LargeEhrCoreMessageListener(tracer, s3ExtendedMessageFetcher, largeEhrCoreMessageHandler));

        connection.start();

        return session;
    }

    @Bean
    public Session createLargeEhrFragmentsQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("ehr small queue name : {}", largeMessageFragmentsQueueName);
        var largeEhrFragmentsConsumer = session.createConsumer(session.createQueue(largeMessageFragmentsQueueName));
        largeEhrFragmentsConsumer.setMessageListener(new LargeMessageFragmentsListener(tracer, s3ExtendedMessageFetcher, largeMessageFragmentHandler));

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

    @Bean
    public Session createNegativeAckQueueListener(SQSConnection connection) throws JMSException {
        Session session = getSession(connection);

        log.info("nack queue name : {}", negativeAckQueueName);
        var nackConsumer = session.createConsumer(session.createQueue(negativeAckQueueName));
        nackConsumer.setMessageListener(new NegativeAcknowledgementListener(tracer, parser, negativeAcknowledgementHandler));

        connection.start();

        return session;
    }

    private Session getSession(SQSConnection connection) throws JMSException {
        return connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);
    }
}
