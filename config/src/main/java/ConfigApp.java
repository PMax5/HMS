import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import services.RabbitMqService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ConfigApp {
    public static void main(String[] args) {

        RabbitMqService rabbitMqService = new RabbitMqService();

        try {
            final String configQueueName = rabbitMqService.getRabbitMqConfig().getConfigQueue();
            Channel channel = rabbitMqService.createNewChannel();
            channel.queueDeclare(configQueueName, true, false, true, null);
            channel.queuePurge(configQueueName);

            System.out.println("Initializing server...");
            // TODO: Refactor this to include real code - add method to create RPCServer and RPCClient
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "Hello world!";

                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProperties, response.getBytes(StandardCharsets.UTF_8));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(configQueueName, false, deliverCallback, (consumerTag -> {}));
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }


    }
}
