package uk.nhs.prm.repo.ehrtransferservice.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

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

    @Test
    public void readMessageShouldCreateMessageBodyFromReadablePartsThatIsIdenticalToTheUnreadableOriginal() throws Exception {
        var unreadableMessage = rawLoader.getDataAsString("MCCI_IN010000UK13FailureSanitized");

        var messageFromReadableLoader = readableHandler.readMessage("MCCI_IN010000UK13", "FailureSanitized");

        assertThat(messageFromReadableLoader).isEqualTo(unreadableMessage);
    }

    private static String prettyPrint(Document doc) {
        return transform(doc, xmlTransformer -> xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes"));
    }

    private static String stripWhitespace(Document doc) {
        return transform(doc, xmlTransformer -> xmlTransformer.setOutputProperty(OutputKeys.INDENT, "no"));
    }

    private static String transform(Document doc, Consumer<Transformer> configurer) {
        var outputStream = new ByteArrayOutputStream();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            final Transformer transformer = transformerFactory.newTransformer();

            configurer.accept(transformer);

            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
            return outputStream.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void configureForPrettyPrint(Transformer transformer) {
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    }
}