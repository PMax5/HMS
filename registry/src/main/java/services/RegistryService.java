package services;

import com.rabbitmq.client.Channel;
import models.Config;
import models.Operations;
import models.RpcClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryService {

    private final RabbitMqService rabbitMqService;
    private final Config config;

    public RegistryService(Config config) {
        this.rabbitMqService = new RabbitMqService();
        this.config = config;
    }

    public void loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        RpcClient rpcClient = new RpcClient(null, configQueueName);
        Channel channel = rabbitMqService.createNewChannel();

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(this.config.getServiceId())
                .build();

        final byte[] response = rpcClient.sendRequest(configQueueName, channel, Operations.CONFIG_REQUEST, configRequest.toByteArray());
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
