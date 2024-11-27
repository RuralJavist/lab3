package org.itmo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.itmo.rabbit.RabbitMqConfig;
import org.itmo.rabbit.RabbitMqProducer;

@Slf4j
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        RabbitMqProducer rabbitMqProducer = new RabbitMqProducer();
        rabbitMqProducer.startProducer();
        Long finishTime = rabbitMqProducer.waitAnswerFromConsumers();
        log.info("Finish time: {} ms", finishTime);
        RabbitMqConfig.closeConnection();
    }
}
