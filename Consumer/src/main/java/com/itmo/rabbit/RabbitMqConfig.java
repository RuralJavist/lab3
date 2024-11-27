package com.itmo.rabbit;

import com.rabbitmq.client.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Getter
public class RabbitMqConfig {

    private final Channel channel;
    private final Connection connection;

    @SneakyThrows
    private RabbitMqConfig(){
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setUsername("makar");
        connectionFactory.setPassword("12345");
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.confirmSelect();
        channel.addReturnListener(new CustomReturnListener());
    }

    private final static RabbitMqConfig INSTANCE = new RabbitMqConfig();

    public static RabbitMqConfig getInstance(){
        return INSTANCE;
    }

    @SneakyThrows
    public static void closeConnection(){
        INSTANCE.channel.close();
        INSTANCE.connection.close();
    }

    @Slf4j
    static class CustomReturnListener implements ReturnListener {

        @Override
        public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
            log.warn(String.format("Message returning: %s", new String(body)));
        }
    }

}
