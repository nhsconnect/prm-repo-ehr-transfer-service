package uk.nhs.prm.repo.ehrtransferservice.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadableTestDataHandlerTest {

    private TestDataLoader rawLoader = new TestDataLoader();
    private ReadableTestDataHandler readableHandler = new ReadableTestDataHandler();

    @Test
    public void splitMessageShouldCreateReadableXmlEnvelopeFromRawMessage_soThatItCanBeUsedToMakeTestDataReadable() throws Exception {
        readableHandler.splitMessage("MCCI_IN010000UK13FailureSanitized");

        var readableEnvelope = rawLoader.getDataAsString("readable/MCCI_IN010000UK13/FailureSanitized/envelope.xml");
        assertThat(readableEnvelope).contains("</soap:Envelope>");

        var readableContent = rawLoader.getDataAsString("readable/MCCI_IN010000UK13/FailureSanitized/payload.xml");
        assertThat(readableContent).contains("<hl7:qualifier code=\"ER\"/>");
    }

    @Disabled("WIP")
    @Test
    public void readMessageShouldCreateMessageBodyFromReadablePartsThatIsIdenticalToTheUnreadableOriginal() throws Exception {
        var unreadableMessage = rawLoader.getDataAsString("MCCI_IN010000UK13FailureSanitized");

        var messageFromReadableLoader = readableHandler.readMessage("MCCI_IN010000UK13", "FailureSanitized");

        assertThat(messageFromReadableLoader).isEqualTo(unreadableMessage);
    }

}