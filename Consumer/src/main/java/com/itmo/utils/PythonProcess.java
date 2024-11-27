package com.itmo.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class PythonProcess {

    private static final String PYTHON_ANALYSER_PATH = "/home/makar/IdeaProjects/ITMO/parallel/lab3-java-RuralJavist/Consumer/src/main/resources/analysis_package/sentiment_analysis.py";

    @SneakyThrows
    public static String execPythonTextAnalyzer(String message) {
        if (message.isBlank()){
            throw new RuntimeException("Message is blank");
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        processBuilder.command("python", PYTHON_ANALYSER_PATH, message);

        Process process = processBuilder.start();
        InputStreamReader streamReader = new InputStreamReader(process.getInputStream());

        process.waitFor();
        String resultText =  new BufferedReader(streamReader).readLine();
        process.destroy();
        return resultText;
    }
}

