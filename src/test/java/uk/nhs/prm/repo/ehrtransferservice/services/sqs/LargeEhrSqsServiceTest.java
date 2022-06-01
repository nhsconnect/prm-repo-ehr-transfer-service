package uk.nhs.prm.repo.ehrtransferservice.services.sqs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.S3PointerMessageParser;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LargeEhrSqsServiceTest {

    @Mock
    private S3PointerMessageParser s3PointerMessageParser;
    @Mock
    private S3PointerMessageHandler s3PointerMessageHandler;
    @InjectMocks
    private LargeEhrSqsService largeEhrSqsService;

    @Test
    void shouldCallS3PointerMessageParserAndS3PointerMessageHandler() throws IOException {
        largeEhrSqsService.getLargeEhrMessage("sqs payload");
        verify(s3PointerMessageParser, times(1)).parse("sqs payload");
        verify(s3PointerMessageHandler, times(1)).handle(any());
    }

}