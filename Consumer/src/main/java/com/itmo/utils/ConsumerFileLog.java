package com.itmo.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j
public class ConsumerFileLog {

    private final static String DIRECTORY_NAME = "logs";
    private final static String FILE_NAME = "log-";

    @SneakyThrows
    public void logResultsInFile(byte[] logByteArr, Integer timePoint){
        String logDirectory = checkDirectory();
        Path filePath = Files.createFile(Paths.get(String.format("%s/%s%s", logDirectory, FILE_NAME, timePoint)));
        OutputStream fileOutputStream = Files.newOutputStream(filePath);
        fileOutputStream.write(logByteArr);
        fileOutputStream.close();
    }

    @SneakyThrows
    public void dropFile(){
        String logDirectory = checkDirectory();
        if (!logDirectory.isBlank()) {
           DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(logDirectory));
           directoryStream.forEach(file -> {
               try {
                   Files.delete(file.toAbsolutePath());
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           });
           directoryStream.close();
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
