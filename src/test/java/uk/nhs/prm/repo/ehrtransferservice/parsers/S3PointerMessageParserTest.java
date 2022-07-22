package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.S3PointerMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3PointerMessageParserTest {

    @Test
    void shouldParseAValidMessage() {
        var validMessage = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\",{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}]";
        S3PointerMessage parseMessage = new S3PointerMessageParser().parse(validMessage);
        assertThat(parseMessage.getS3BucketName()).isEqualTo("s3-bucket-name");
        assertThat(parseMessage.getS3Key()).isEqualTo("s3-key-value");
    }
    @Test
    void shouldThrowAnExceptionWhenTryingToParseAnInValidMessage() {
        var inValid = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\"]";
        assertThrows(Exception.class,() -> new S3PointerMessageParser().parse(inValid));
    }
}