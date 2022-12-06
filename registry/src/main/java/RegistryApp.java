import com.rabbitmq.client.Channel;
import models.RpcClient;
import services.ConfigService;
import services.RabbitMqService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        ConfigService configService = new ConfigService();
        RabbitMqService rabbitMqService = new RabbitMqService();

        try {
            String configQueueName = rabbitMqService.getRabbitMqConfig().getConfigQueue();
            RpcClient rpcClient = new RpcClient(null, configQueueName);
            Channel channel = rabbitMqService.createNewChannel();
            final byte[] response = rpcClient.sendRequest(configQueueName, channel, "Hello".getBytes(StandardCharsets.UTF_8));

        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            // TODO: Create specific exceptions and logger.
            e.printStackTrace();
        }
    }
}