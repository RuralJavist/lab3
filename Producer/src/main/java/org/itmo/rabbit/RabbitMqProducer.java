package org.itmo.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.itmo.pojo.Message;
import org.itmo.utils.ProducerFileLog;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Slf4j
public class RabbitMqProducer {

    private final static int BUFFER_SIZE = 16480;
    private final static String ROUTING_KEY_CONSUMER = "consumer-1";
    private final static String ROUTING_KEY_PRODUCER = "producer";
    private final static String TEXT_REGEX_TOPIC = "Chapter\\s\\d+";
    private final static String FILE_PATH = "/home/makar/IdeaProjects/ITMO/parallel/lab3-java-RuralJavist/Producer/src/main/resources/pride-and-prejudice.txt";
    private List<Integer> indexMap;
    private Instant start;
    private final ProducerFileLog fileLog = new ProducerFileLog();

    @SneakyThrows
    public void startProducer() {
        fileLog.dropFile();
        start = Instant.now();
        indexMap = new ArrayList<>();
        FileChannel fileChannel = FileChannel.open(Paths.get(FILE_PATH));
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
        StringBuilder textBuffer = new StringBuilder();
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        if (byteBuffer.hasRemaining()) {
            textBuffer.append(new String(byteBuffer.array()));
            String finalTextForProcessing = textBuffer.toString();
            if (finalTextForProcessing.isEmpty()) {
                log.warn("Warning: can`t read, text file in empty!");
            } else {
                findRegexIndices(finalTextForProcessing, TEXT_REGEX_TOPIC, indexMap);
                for (int i = 0; i <= indexMap.size()-1; i++) {
                    Message message;
                    if (i == indexMap.size()-1) {
                        message = new Message(finalTextForProcessing.substring(indexMap.get(i)), i+1);
                     } else {
                        message = new Message(finalTextForProcessing.substring(indexMap.get(i), indexMap.get(i+1)), i+1);
                    }
                    sendMessage(message);
                }
            }
        } else {
            log.warn("Warning: can`t read, file is empty!");
        }
        fileChannel.close();
    }

    @SneakyThrows
    public Long waitAnswerFromConsumers(){
        Channel channel = RabbitMqConfig.getInstance().getChannel();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StringBuilder resultTextBuilder = new StringBuilder();
        List<Message> resultMessages = new ArrayList<>();
        channel.basicConsume(ROUTING_KEY_PRODUCER, true, new RabbitMqResponseConsumer(resultMessages, countDownLatch, indexMap.size()));
        countDownLatch.await();
        long resultTime = Duration.between(start, Instant.now()).toMillis();
        resultTextBuilder.insert(0, String.format("Result time: %s mls. Time stamp: %s\n\n", resultTime, LocalDateTime.now()));
        for (Message message : resultMessages) {
            resultTextBuilder.append(message.getMessage());
        }
        fileLog.logResultsInFile(resultTextBuilder.toString().getBytes());
        return Duration.between(start, Instant.now()).getSeconds();
    }

    private void findRegexIndices(String text, String regex, List<Integer> indexMap) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            indexMap.add(matcher.start());
        }
    }

    @SneakyThrows
    private void sendMessage(Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        RabbitMqConfig.getInstance().getChannel().basicPublish("", ROUTING_KEY_CONSUMER, true, null, objectMapper.writeValueAsBytes(message));
    }

}
