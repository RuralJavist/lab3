package org.itmo.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;
import org.itmo.pojo.Message;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RabbitMqResponseConsumer extends DefaultConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private Instant lastProcessedMessage;
    private final List<Message> resultMessages;
    private final CountDownLatch latch;
    private final int awaitSize;
    private int counter = 0;

    public RabbitMqResponseConsumer(List<Message> resultMessages, CountDownLatch latch, Integer awaitSize) {
        super(RabbitMqConfig.getInstance().getChannel());
        this.resultMessages = resultMessages;
        this.latch = latch;
        this.awaitSize = awaitSize;
    }
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (awaitSize-1 == counter) {
            resultMessages.sort(Comparator.comparing(Message::getUniqueId));
            latch.countDown();
        }
        lastProcessedMessage = Instant.now();
        Message message = objectMapper.readValue(body, Message.class);
        resultMessages.add(message);
        counter++;

    }
}
