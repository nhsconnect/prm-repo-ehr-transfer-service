package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.json_models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class S3PointerMessageHandlerTest {
    @Mock
    private AmazonS3 s3client;
    @Mock
    private Parser parser;

    @InjectMocks
    private S3PointerMessageHandler s3PointerMessageHandler;

    @Test
    void shouldCallParserToParseMessageReturnedFromS3() throws IOException {
        var s3PointerMessage = getStaticS3PointerMessage();
        var s3Object = new S3Object();
        s3Object.setObjectContent(readResourceFile());
        when(s3client.getObject(anyString(), anyString())).thenReturn(s3Object);
        s3PointerMessageHandler.handle(getStaticS3PointerMessage());
        verify(s3client).getObject(s3PointerMessage.getS3BucketName(), s3PointerMessage.getS3Key());
        verify(parser, times(1)).parse(any());

    }

    private S3PointerMessage getStaticS3PointerMessage() {
        var validMessage = "{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}";
        var json = (JsonObject) JsonParser.parseString(validMessage);
        return new S3PointerMessage(json);
    }

    private InputStream readResourceFile() throws FileNotFoundException {
        InputStream targetStream = new FileInputStream("src/test/resources/data/RCMR_IN010000UK05");
        return targetStream;
    }

}