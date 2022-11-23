package services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import models.RabbitMqConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqService {

    private final RabbitMqConfig rabbitMqConfig;
    private final ConnectionFactory connectionFactory;

    public RabbitMqService() {
        this.rabbitMqConfig = new RabbitMqConfig(
                    System.getenv("RABBITMQ_ADDRESS"),
                    System.getenv("RABBITMQ_PORT"),
                    System.getenv("RABBITMQ_USERNAME"),
                    System.getenv("RABBITMQ_PASSWORD"),
                    System.getenv("RABBITMQ_VIRTUALHOST"),
                    System.getenv("RABBITMQ_CONFIG_QUEUE")
            );

        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setUsername(this.rabbitMqConfig.getRabbitMqUserName());
        this.connectionFactory.setPassword(this.rabbitMqConfig.getRabbitMqPassword());
        this.connectionFactory.setVirtualHost(this.rabbitMqConfig.getVirtualHost());
        this.connectionFactory.setHost(this.rabbitMqConfig.getRabbitMqAddress());
        this.connectionFactory.setPort(Integer.parseInt(this.rabbitMqConfig.getRabbitMqPort()));
    }

    public Channel createNewChannel() throws IOException, TimeoutException {
        Connection connection = this.connectionFactory.newConnection();

        return connection.createChannel();
    }
}
