import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import models.Operations;
import models.RpcServer;
import services.RabbitMqService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ConfigApp {
    public static void main(String[] args) {

        RabbitMqService rabbitMqService = new RabbitMqService();

        try {
            final String configQueueName = rabbitMqService.getRabbitMqConfig().getConfigQueue();
            RpcServer configServer = rabbitMqService.newRpcServer(configQueueName);
            Channel channel = configServer.getChannel();

            System.out.println("Initializing server...");
            configServer.addOperationHandler(Operations.CONFIG_REQUEST, (consumerTag, delivery) -> {
                configServer.sendResponseAndAck(delivery, "Hello".getBytes(StandardCharsets.UTF_8));
            });

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                configServer.executeOperationHandler(delivery);
            };

            channel.basicConsume(configQueueName, false, mainHandler, (consumerTag -> {}));
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
