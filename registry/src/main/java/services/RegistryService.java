package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import models.Config;
import models.Operations;
import models.RpcClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryService {

    private final RabbitMqService rabbitMqService;
    private final static String SERVICE_ID = "service_registry";
    private Config config;

    public RegistryService() {
        this.rabbitMqService = new RabbitMqService();
    }

    public void loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        RpcClient rpcClient = new RpcClient(null, configQueueName);
        Channel channel = rabbitMqService.createNewChannel();

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(SERVICE_ID)
                .build();

        final byte[] response = rpcClient.sendRequest(
                configQueueName,
                channel,
                Operations.CONFIG_REQUEST,
                configRequest.toByteArray()
        );

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
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
