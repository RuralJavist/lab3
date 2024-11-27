package org.itmo.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ProducerFileLog {

    private final static String DIRECTORY_NAME = "logs";
    private final static String FILE_NAME = "result";

    @SneakyThrows
    public void logResultsInFile(byte[] logByteArr){
        String logDirectory = checkDirectory();
        Path filePath = Files.createFile(Paths.get(String.format("%s/%s", logDirectory, FILE_NAME)));
        OutputStream fileOutputStream = Files.newOutputStream(filePath);
        fileOutputStream.write(logByteArr);
        fileOutputStream.close();
    }

    @SneakyThrows
    public void dropFile(){
        String logDirectory = checkDirectory();
        Path path = Paths.get(String.format("%s/%s", logDirectory, FILE_NAME));
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    private String checkDirectory(){
        URL logDirectory = this.getClass().getClassLoader().getResource(DIRECTORY_NAME);
        if (logDirectory == null) {
            log.warn("Log directory not found");
        }
        return logDirectory.getPath();
    }

}
