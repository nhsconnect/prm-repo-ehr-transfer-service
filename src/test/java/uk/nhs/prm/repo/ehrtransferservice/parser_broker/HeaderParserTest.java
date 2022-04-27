package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;

public class HeaderParserTest {
    HeaderParser headerParser = new HeaderParser();
    private final TestDataLoader loader = new TestDataLoader();

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN030000UK06, ff27abc3-9730-40f7-ba82-382152e6b90a",
            "COPC_IN000001UK01, ff1457fb-4f58-4870-8d90-24d9c3ef8b91",
            "PRPA_IN000202UK01, 723c5f3a-1ab8-4515-a582-3e5cc600bf59",
            "RCMR_IN010000UK05, 17a757f2-f4d2-444e-a246-9cb77bef7f22",
    })
    void shouldGetCorrelationId(String rawMessageFile, String correlationId) throws IOException {
        byte[] rawMessageFromQueue = loader.getDataAsBytes(rawMessageFile);
        Assertions.assertEquals(headerParser.getCorrelationId(rawMessageFromQueue), correlationId);
    }
}
