package uk.nhs.prm.repo.ehrtransferservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadableTestDataHandler {

    private TestDataLoader rawLoader = new TestDataLoader();

    public String readMessage(String interactionId, String variant) {
        Path messageFolder = readableMessageDirPath(interactionId, variant);

        Path envelopeXmlFile = messageFolder.resolve("envelope.xml");
        Path contentXmlFile = messageFolder.resolve("payload.xml");

        // serialize to json

        return interactionId;
    }

    public void splitMessage(String unreadableFilename) {
        var rawMessage = readRawDataResource(unreadableFilename);

        var interactionId = unreadableFilename.substring(0, 17);
        var variant = unreadableFilename.substring(17);

        Path messageFolder = readableMessageDirPath(interactionId, variant);

        Path envelopeXmlFile = messageFolder.resolve("envelope.xml");
        Path contentXmlFile = messageFolder.resolve("payload.xml");

        var parsedJson = parseMhsMessage(rawMessage);

        writeToFile(envelopeXmlFile, parsedJson.ebXML);
        writeToFile(contentXmlFile, parsedJson.payload);
    }

    private Path readableMessageDirPath(String interactionId, String variant) {
        return Paths.get(dataDirPath().toAbsolutePath().toString(), "readable", interactionId, variant);
    }

    private Path dataDirPath() {
        return getPath("data/");
    }

    private MhsJsonMessage parseMhsMessage(String rawMessage) {
        try {
            return new ObjectMapper().readValue(rawMessage, MhsJsonMessage.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readRawDataResource(String unreadableFilename) {
        try {
            return rawLoader.getDataAsString(unreadableFilename);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(Path filepath, String content) {
        try {
            System.out.println("writing to " + filepath);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, content.getBytes());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getPath(String path) {
        Path readableDir = null;
        try {
            readableDir = ResourceUtils.getFile("classpath:" + path).toPath();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return readableDir;
    }
}
