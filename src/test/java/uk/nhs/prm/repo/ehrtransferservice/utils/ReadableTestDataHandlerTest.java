package uk.nhs.prm.repo.ehrtransferservice.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReadableTestDataHandlerTest {

    private TestDataLoader rawLoader = new TestDataLoader();
    private ReadableTestDataHandler readableHandler = new ReadableTestDataHandler();

    @Test
    public void splitMessageShouldCreateReadableXmlEnvelopeFromRawMessage_soThatItCanBeUsedToMakeTestDataReadable() throws Exception {
        readableHandler.splitMessage("readabletestdataXReadableTestDataHandlerTestData");

        var readableEnvelope = rawLoader.getDataAsString("readable/readabletestdataX/ReadableTestDataHandlerTestData/envelope.xml");
        assertThat(readableEnvelope).contains("</soap:Envelope>");

        var readableContent = rawLoader.getDataAsString("readable/readabletestdataX/ReadableTestDataHandlerTestData/payload.xml");
        assertThat(readableContent).contains("<hl7:qualifier code=\"ER\"/>");
    }

    @Test
    public void readMessageShouldCreateMessageBodyFromReadablePartsThatIsIdenticalToTheUnreadableOriginal() throws Exception {
        var unreadableMessage = rawLoader.getDataAsString("readabletestdataXReadableTestDataHandlerTestData");

        var messageFromReadableLoader = readableHandler.readMessage("readabletestdataX", "ReadableTestDataHandlerTestData");

        assertThat(messageFromReadableLoader).isEqualTo(unreadableMessage);
    }
}