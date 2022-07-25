package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.models.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;

class S3PointerMessageParserTest {

    @Test
    void shouldParseAValidMessage() {
        var validMessage = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\",{\"s3BucketName\":\"s3-bucket-name\",\"s3Key\":\"s3-key-value\"}]";

        var result = new S3PointerMessageParser().parse(validMessage);

        assertThat(result.getStatus()).isEqualTo(Status.OK);
        assertThat(result.getMessage().getS3BucketName()).isEqualTo("s3-bucket-name");
        assertThat(result.getMessage().getS3Key()).isEqualTo("s3-key-value");
    }
    @Test
    void shouldNotSucceedWhenTryingToParseAnInValidMessage() {
        var inValid = "[\"software.amazon.payloadoffloading.PayloadS3Pointer\"]";

        var result = new S3PointerMessageParser().parse(inValid);

        assertThat(result.getStatus()).isEqualTo(Status.KO);
        assertThat(result.getMessage()).isNull();
    }
}