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
    
    @MethodSource("testDataFilesAndNames")
    @ParameterizedTest
    public void shouldBeAbleToReadMessagesFromReadableFormatToBeIdenticalToOriginals(String original, String interactionId, String variant) throws Exception {
        var unreadableMessage = rawLoader.getDataAsString(original);

        var messageFromReadableLoader = readableHandler.readMessage(interactionId, variant);

        assertThat(messageFromReadableLoader).isEqualTo(unreadableMessage);
    }

    private static Stream<Arguments> testDataFilesAndNames() {
        return Stream.of(
                Arguments.of("MCCI_IN010000UK13FailureSanitized", "MCCI_IN010000UK13", "FailureSanitized"),
                Arguments.of("MCCI_IN010000UK13Empty", "MCCI_IN010000UK13", "Empty"),
                Arguments.of("PRPA_IN000202UK01Sanitized", "PRPA_IN000202UK01", "Sanitized"),
                Arguments.of("RCMR_IN010000UK05Sanitized", "RCMR_IN010000UK05", "Sanitized"),
                Arguments.of("RCMR_IN030000UK06Sanitized", "RCMR_IN030000UK06", "Sanitized"),
                Arguments.of("RCMR_IN030000UK06SanitizedWithUnexpectedBackslash", "RCMR_IN030000UK06", "SanitizedWithUnexpectedBackslash"),
                Arguments.of("RCMR_IN030000UK06WithMidSanitized", "RCMR_IN030000UK06", "WithMidSanitized")
        );
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