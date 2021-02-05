package uk.nhs.prm.deductions.gp2gpmessagehandler.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestDataLoader {
    public String getData(String fileName) throws IOException {
        File file = ResourceUtils.getFile(String.format("classpath:data/%s", fileName));
        return new String(Files.readAllBytes(file.toPath()));
    }
}
