package uk.nhs.prm.deductions.gp2gpmessagehandler.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TestDataLoader {
    public String getData(String fileName) throws IOException {
        File file = ResourceUtils.getFile(String.format("classpath:data/%s", fileName));
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
