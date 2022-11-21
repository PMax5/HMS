import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import models.RabbitMqConfig;
import services.ConfigService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // Load RabbitMQ config
        ConfigService configService = new ConfigService();
        RabbitMqConfig rabbitMqConfig = configService.loadRabbitMqConfig();

        // Connect to RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(rabbitMqConfig.getRabbitMqUserName());
        factory.setPassword(rabbitMqConfig.getRabbitMqPassword());
        factory.setVirtualHost(rabbitMqConfig.getVirtualHost());
        factory.setHost(rabbitMqConfig.getRabbitMqAddress());
        factory.setPort(Integer.parseInt(rabbitMqConfig.getRabbitMqPort()));

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare().getQueue();
            

        } catch (IOException | TimeoutException e) {
            // TODO: Create specific exceptions and logger.
            e.printStackTrace();
        }
    }
}