package com.itmo;

import com.itmo.rabbit.RabbitMqConfig;
import com.itmo.rabbit.RabbitMqConsumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.concurrent.*;

public class Main {

    private final static Integer CONSUMER_MESSAGE_DELAY = 30000;
    private final static Integer CHECKER_TIMEOUT_DELAY = 300;
    private final static ExecutorService CONSUMER_EXECUTOR = Executors.newSingleThreadExecutor();
    private final static ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private final static String ROUTING_KEY_CONSUMER = "consumer-1";

    @SneakyThrows
    public static void main(String[] args) {
        RabbitMqConsumer rabbitMqConsumer = new RabbitMqConsumer();
        rabbitMqConsumer.getChannel().basicConsume(ROUTING_KEY_CONSUMER, true, rabbitMqConsumer);
        new InterruptWrapper(new CheckMessageTimeout(rabbitMqConsumer)).runNTimes();
        CONSUMER_EXECUTOR.shutdown();
        TIMEOUT_EXECUTOR.shutdown();

        if (!CONSUMER_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("CONSUMER_EXECUTOR did not terminate in time.");
        }
        if (!TIMEOUT_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("TIMEOUT_EXECUTOR did not terminate in time.");
        }
        RabbitMqConfig.closeConnection();
        System.exit(0);
    }

    @RequiredArgsConstructor
    static class CheckMessageTimeout implements Callable<Boolean> {

        private final RabbitMqConsumer rabbitMqConsumer;

        @SneakyThrows
        @Override
        public Boolean call() {
            Instant lastDateTime = rabbitMqConsumer.getLastMessageProcessingTime().plusMillis(CONSUMER_MESSAGE_DELAY);
            Instant now = Instant.now();
            return now.isAfter(lastDateTime);
        }
    }

    @RequiredArgsConstructor
    static class InterruptWrapper implements Runnable{
        private final Callable<Boolean> delegate;
        private ScheduledFuture<?> future;

        @SneakyThrows
        @Override
        public void run() {
            if(delegate.call()) {
                future.cancel(true);
            }
        }

        @SneakyThrows
        public void runNTimes() {
             future = TIMEOUT_EXECUTOR.scheduleWithFixedDelay(this, 0, CHECKER_TIMEOUT_DELAY, TimeUnit.MILLISECONDS);
             try {
                 future.get();
             } catch (CancellationException e){
                 return;
             } catch (InterruptedException | ExecutionException e) {
                 e.printStackTrace();
             }
        }
    }

}