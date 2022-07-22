package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.S3PointerMessageParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3PointerMessageHandlerTest {
    @Mock
    private S3Client s3Client;
    @Mock
    private S3PointerMessageParser s3PointerMessageParser;

    @InjectMocks
    private S3PointerMessageHandler s3PointerMessageHandler;

    @Test
    void shouldCallParserToParseMessageReturnedFromS3() throws IOException {
        mockS3GetObjectResponseToReturnContentFrom("RCMR_IN030000UK06Sanitized");
        s3PointerMessageHandler.getLargeSqsMessage(getStaticS3PointerMessage());
        verify(s3Client).getObject(GetObjectRequest.builder().bucket("s3-bucket-name").key("s3-key-value").build());
    }

    @Test
    void shouldThrowExceptionWhenS3MessageIsNotValid() {
        mockS3GetObjectResponseToReturnContentFrom("simpleTextMessage.txt");
        assertThrows(JsonProcessingException.class, () -> s3PointerMessageHandler.getLargeSqsMessage(getStaticS3PointerMessage()));
    }

    @Test
    void shouldCallS3PointerMessageParserWithS3PointerPayLoad() throws IOException {
        var payload = "{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}";
        mockS3GetObjectResponseToReturnContentFrom("RCMR_IN030000UK06Sanitized");
        when(s3PointerMessageParser.parse(any())).thenReturn(getStaticS3PointerMessage());
        s3PointerMessageHandler.getLargeSqsMessage(payload);
        verify(s3PointerMessageParser).parse(payload);
    }

    @Disabled("Checking if functionality fixed in app first")
    @Test
    void shouldNotCallS3PointerMessageParserWithoutS3PointerPayLoad() throws IOException {
        var payload = "TBD?";
        when(s3PointerMessageParser.parse(any())).thenReturn(getStaticS3PointerMessage());
        s3PointerMessageHandler.getLargeSqsMessage(payload);
        verify(s3PointerMessageParser, never()).parse(payload);
    }

    private S3PointerMessage getStaticS3PointerMessage() {
        var validMessage = "{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}";
        var json = (JsonObject) JsonParser.parseString(validMessage);
        return new S3PointerMessage(json);
    }

    private void mockS3GetObjectResponseToReturnContentFrom(String resourceFileName) {
        when(s3Client.getObject(any(GetObjectRequest.class))).then(invocation -> {
            GetObjectRequest getObjectRequest = invocation.getArgument(0);
            assertEquals("s3-bucket-name", getObjectRequest.bucket());
            assertEquals("s3-key-value", getObjectRequest.key());

            return new ResponseInputStream<>(GetObjectResponse.builder().build(), AbortableInputStream.create(readResourceFile(resourceFileName)));
        });
    }

    @SuppressFBWarnings
    private InputStream readResourceFile(String resourceFileName) throws FileNotFoundException {
        return new FileInputStream("src/test/resources/data/" + resourceFileName);
    }
}