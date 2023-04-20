package services;

import com.rabbitmq.client.*;
import models.RabbitMqConfig;
import models.RpcServer;

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

        String virtualHost = this.rabbitMqConfig.getVirtualHost();
        if (virtualHost != null && !virtualHost.equals(""))
            this.connectionFactory.setVirtualHost(virtualHost);
        this.connectionFactory.setHost(this.rabbitMqConfig.getRabbitMqAddress());
        this.connectionFactory.setPort(Integer.parseInt(this.rabbitMqConfig.getRabbitMqPort()));
    }

    public Channel createNewChannel() throws IOException, TimeoutException {
        Connection connection = this.connectionFactory.newConnection();

        return connection.createChannel();
    }

    public RpcServer newRpcServer(String queueName) throws IOException, TimeoutException {
        Channel channel = this.createNewChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queuePurge(queueName);
        return new RpcServer(channel, queueName);
    }

    public RpcClient newRpcClient(RpcClientParams rpcClientParams) throws IOException {
        return new RpcClient(rpcClientParams);
    }

    public RabbitMqConfig getRabbitMqConfig() {
        return rabbitMqConfig;
    }
}
