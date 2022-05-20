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
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.listeners.SmallEhrMessageListener;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventListener;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEventParser;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingService;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqsListenerSpringConfiguration {

    private final Tracer tracer;
    private final RepoIncomingService repoIncomingService;
    private final RepoIncomingEventParser repoIncomingEventParser;
    private final SmallEhrMessageHandler smallEhrMessageHandler;

    private final Parser parser;

    @Value("${aws.repoIncomingQueueName}")
    private String repoIncomingQueueName;

    @Value("${aws.smallEhrQueueName}")
    private String smallEhrQueueName;

/*
    @Value("${aws.ehrCompleteQueueName}")
    private String ehrCompleteQueueName;
*/

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
    public Session createListeners(SQSConnection connection) throws JMSException {
        var session = connection.createSession(false, SQSSession.UNORDERED_ACKNOWLEDGE);

        log.info("repo incoming queue name : {}", repoIncomingQueueName);
        var incomingQueueConsumer = session.createConsumer(session.createQueue(repoIncomingQueueName));
        incomingQueueConsumer.setMessageListener(new RepoIncomingEventListener(tracer, repoIncomingService, repoIncomingEventParser));

        log.info("ehr small queue name : {}", smallEhrQueueName);
        var ehrSmallConsumer = session.createConsumer(session.createQueue(smallEhrQueueName));
        ehrSmallConsumer.setMessageListener(new SmallEhrMessageListener(tracer, parser, smallEhrMessageHandler));

/*
      log.info("ehr complete queue name : {}", ehrCompleteQueueName);
      var ehrCompleteConsumer = session.createConsumer(session.createQueue(ehrCompleteQueueName));
      ehrCompleteConsumer.setMessageListener(new EhrCompleteMessageListener(tracer, parser));
*/

        connection.start();

        return session;
    }
}
