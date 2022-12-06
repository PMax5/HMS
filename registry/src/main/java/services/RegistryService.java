package services;

import com.rabbitmq.client.Channel;
import models.RpcClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryService {

    private final RabbitMqService rabbitMqService;

    public RegistryService() {
        this.rabbitMqService = new RabbitMqService();
    }

    public void loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        RpcClient rpcClient = new RpcClient(null, configQueueName);
        Channel channel = rabbitMqService.createNewChannel();

        final byte[] response = rpcClient.sendRequest(configQueueName, channel, "Hello".getBytes(StandardCharsets.UTF_8));
    }

    public void registerUser() {
        // TODO: Register user
    }

    public void authenticateUser() {
        // TODO: Authenticate user
    }

    public boolean authorizeUser(String userToken) {
        // TODO: Validate user token
        return false;
    }

    public void registerService(String serviceId) {
        // TODO: Register service
    }

    public boolean authenticateService(String serviceToken) {
        // TODO: Authenticate service
        return false;
    }
}
