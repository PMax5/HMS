package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryService {

    private final RabbitMqService rabbitMqService;
    private final HyperledgerService hyperledgerService;
    private final static String SERVICE_ID = "service_registry";
    private Config config;

    public RegistryService(RabbitMqService rabbitMqService) throws Exception {
        this.rabbitMqService = rabbitMqService;
        this.hyperledgerService = new HyperledgerService();
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel), configQueueName);

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

        // TODO: Fetch config from service.
        // this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
        this.config = new Config(SERVICE_ID);

        return this.config;
    }

    public void loadHyperledgerService() throws Exception {
        this.hyperledgerService.enrollAdminUser();
        // TODO: Load Hyperledger Fabric users
    }

    public User registerUser(String username, String name, int age, Gender gender, UserRole userRole, String hashedPassword) {
        try {
            return this.hyperledgerService.registerUser(
                    username,
                    name,
                    age,
                    gender,
                    userRole,
                    hashedPassword
            );
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Registry Service] Failed to register user: " + e.getMessage());
            return null;
        }
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
