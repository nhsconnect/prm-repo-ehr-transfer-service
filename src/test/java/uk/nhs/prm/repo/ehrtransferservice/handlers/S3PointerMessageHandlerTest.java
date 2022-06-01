package uk.nhs.prm.repo.ehrtransferservice.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3PointerMessageHandlerTest {
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3PointerMessageHandler s3PointerMessageHandler;

    @Test
    void shouldCallParserToParseMessageReturnedFromS3() throws IOException {
        mockS3GetObjectResponseToReturnContentFrom("RCMR_IN030000UK06Sanitized");
        s3PointerMessageHandler.handle(getStaticS3PointerMessage());
        verify(s3Client).getObject(GetObjectRequest.builder().bucket("s3-bucket-name").key("s3-key-value").build());
    }

    @Test
    void shouldThrowExceptionWhenS3MessageIsNotValid() {
        mockS3GetObjectResponseToReturnContentFrom("simpleTextMessage.txt");
        assertThrows(JsonProcessingException.class, () -> s3PointerMessageHandler.handle(getStaticS3PointerMessage()));
    }

    private S3PointerMessage getStaticS3PointerMessage() {
        var validMessage = "{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}";
        var json = (JsonObject) JsonParser.parseString(validMessage);
        return new S3PointerMessage(json);
    }

    private void mockS3GetObjectResponseToReturnContentFrom(String resourceFileName) {
        when(s3Client.getObject(
                any(GetObjectRequest.class)
        ))
                .then(
                        invocation -> {
                            GetObjectRequest getObjectRequest = invocation.getArgument(0);
                            assertEquals("s3-bucket-name", getObjectRequest.bucket());
                            assertEquals("s3-key-value", getObjectRequest.key());

                            return new ResponseInputStream<>(
                                    GetObjectResponse.builder().build(), AbortableInputStream.create(readResourceFile(resourceFileName)));
                        });
    }

    @SuppressFBWarnings
    private InputStream readResourceFile(String resourceFileName) throws FileNotFoundException {
        InputStream targetStream = new FileInputStream("src/test/resources/data/" + resourceFileName);
        return targetStream;
    }
}