import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import services.ConfigService;
import services.RabbitMqService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        ConfigService configService = new ConfigService();
        RabbitMqService rabbitMqService = new RabbitMqService();

        try {

            Channel channel = rabbitMqService.createNewChannel();
            final String corrId = UUID.randomUUID().toString();
            String replyQueueName = channel.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            channel.basicPublish("", "service_config", props, "TEST".getBytes(StandardCharsets.UTF_8));

            final CompletableFuture<String> response = new CompletableFuture<>();

            String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.complete(new String(delivery.getBody(), StandardCharsets.UTF_8));
                }
            }, consumerTag -> {
            });

            String result = response.get();
            channel.basicCancel(ctag);

            System.out.println(result);

        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            // TODO: Create specific exceptions and logger.
            e.printStackTrace();
        }
    }
}