package com.itmo.utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class TextUtils {

    private final static String START_GOOD_RESPONSE = "Sentiment";
    private final static String WORD_TO_REPLACE = "Mr\\.";
    private final static String REPLACEMENT_WORD = "IPKN";
    private final static String SPLIT_REGEX = "(?<=[.!?\t])\\s+";


    public static Pair<Integer, HashMap<String, Integer>> startWordsProcessing(String text) {
        HashMap<String, Integer> uniqueWordMap = new HashMap<>();
        String[] words = text.split("\\W+");
        int wordCount = 0;
        for (String word : words) {
            String lowerWord = word.toLowerCase();
            wordCount++;
            if (!uniqueWordMap.containsKey(lowerWord)) {
                uniqueWordMap.put(lowerWord, 1);
            } else {
                uniqueWordMap.put(lowerWord, uniqueWordMap.get(lowerWord) + 1);
            }
        }
        return new Pair<>(wordCount, uniqueWordMap);
    }

    public static String startAnalyzerProcessing(String text) {
        String outputText = PythonProcess.execPythonTextAnalyzer(text);
        if (outputText.isBlank() || !outputText.startsWith(START_GOOD_RESPONSE)){
            throw new RuntimeException(String.format("Python analyzer not working! Response: %s", outputText));
        } else {
            String clearResult = outputText.replaceAll(START_GOOD_RESPONSE, "").replaceAll("[()]", "");
            String[] data = clearResult.split(",");
            return data[0];
        }
    }

    public static String replaceWord(String text){
       return text.replaceAll(WORD_TO_REPLACE, REPLACEMENT_WORD);
    }

    public static String orderSentencesByLength(String text){;
        String[] sentences =text.replaceAll("[\\n‘’]", "").split(SPLIT_REGEX);
        Arrays.sort(sentences, new CustomComparator());
        return String.join("\n", sentences);
    }


    static class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }

}
