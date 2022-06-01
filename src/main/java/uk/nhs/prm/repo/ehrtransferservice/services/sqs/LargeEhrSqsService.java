package uk.nhs.prm.repo.ehrtransferservice.services.sqs;

import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.S3PointerMessageParser;

import java.io.IOException;

public class LargeEhrSqsService {

    private S3PointerMessageParser parser;
    private S3PointerMessageHandler s3PointerMessageHandler;

    public LargeEhrSqsService(S3PointerMessageParser parser, S3PointerMessageHandler s3PointerMessageHandler) {
        this.parser = parser;
        this.s3PointerMessageHandler = s3PointerMessageHandler;
    }

    public void getLargeEhrMessage(String sqsPayload) throws IOException {
        s3PointerMessageHandler.handle(parser.parse(sqsPayload));

    }
}
