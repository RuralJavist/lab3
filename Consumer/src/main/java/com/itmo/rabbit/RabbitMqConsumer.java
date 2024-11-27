package com.itmo.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itmo.pojo.Message;
import com.itmo.utils.ConsumerFileLog;
import com.itmo.utils.TextUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import kotlin.Pair;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Getter
public class RabbitMqConsumer extends DefaultConsumer {

    private final ObjectMapper objectMapper;
    private Instant lastMessageProcessingTime = Instant.now();
    private final ConsumerFileLog fileLog = new ConsumerFileLog();
    private final static String ROUTING_KEY_PRODUCER = "producer";

    public RabbitMqConsumer() {
        super(RabbitMqConfig.getInstance().getChannel());
        fileLog.dropFile();
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        lastMessageProcessingTime = Instant.now();
        Message message = objectMapper.readValue(body, Message.class);
        String text = message.getMessage();
        String syntacticСoloring = TextUtils.startAnalyzerProcessing(text);
        String replacementVersion = TextUtils.replaceWord(text);
        String sortResult = TextUtils.orderSentencesByLength(replacementVersion);
        Pair<Integer, HashMap<String, Integer>> wordProcessingResult = TextUtils.startWordsProcessing(text);
        fileLog.logResultsInFile(getTextForLog(syntacticСoloring, sortResult, wordProcessingResult), message.getUniqueId());
        sendMessage(new Message(text, message.getUniqueId()));

    }

    private byte[] getTextForLog(String syntacticСoloring, String sortResult,
                                 Pair<Integer, HashMap<String, Integer>> wordProcessingResult) {

        StringBuilder resultReport = new StringBuilder();
        Set<Map.Entry<String, Integer>> topWordBuilderSet = wordProcessingResult.getSecond().entrySet();
        resultReport.append(String.format("Total number of word: %s\n\n", wordProcessingResult.getFirst()));
        resultReport.append("Word popular top:\n\n");
        topWordBuilderSet.stream().sorted(new CustomParComparator()).limit(5)
                .forEach(el -> {
                    String topWordElem = String.format(String.format("Word: %s, count: %s\n", el.getKey(), el.getValue()));
                    resultReport.append(topWordElem);
                });

        resultReport.append(String.format("\nSyntactic color - %s\n", syntacticСoloring));
        resultReport.append(String.format("\nSort version: \n%s\n", sortResult));
        return resultReport.toString().getBytes();
    }

    static class CustomParComparator implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    @SneakyThrows
    private void sendMessage(Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        RabbitMqConfig.getInstance().getChannel().basicPublish("", ROUTING_KEY_PRODUCER, true, null, objectMapper.writeValueAsBytes(message));
    }
}
