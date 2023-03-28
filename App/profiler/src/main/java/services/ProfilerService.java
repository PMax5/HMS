package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ProfilerService {
    private final RabbitMqService rabbitMqService;
    private final String serviceId;
    private HyperledgerService hyperledgerService;

    public ProfilerService(String serviceId, RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
        this.serviceId = serviceId;
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel), configQueueName);

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(this.serviceId)
                .build();

        final byte[] response = rpcClient.sendRequest(
                configQueueName,
                channel,
                Operations.CONFIG_REQUEST,
                configRequest.toByteArray()
        );
        rpcClient.close();

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        System.out.println(configResponse.getServiceConfig());
        return new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
    }

    public void loadHyperLedgerService(Config config) throws Exception {
        this.hyperledgerService = new HyperledgerService(config);
    }

    public UserRole authorizeUser(String token) {
        try {
            String registryQueueName = "service_registry";
            Channel channel = this.rabbitMqService.createNewChannel();
            RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel), registryQueueName);

            Auth.UserAuthorizationRequest authorizationRequest = Auth.UserAuthorizationRequest.newBuilder()
                    .setToken(token)
                    .build();

            final byte[] response = rpcClient.sendRequest(
                    registryQueueName,
                    channel,
                    Operations.AUTHORIZATION_REQUEST,
                    authorizationRequest.toByteArray()
            );
            rpcClient.close();

            Auth.UserAuthorizationResponse authorizationResponse = Auth.UserAuthorizationResponse.parseFrom(response);
            if (authorizationResponse.hasErrorMessage()) {
                throw new Exception(authorizationResponse.getErrorMessage().getDescription());
            }

            return UserRole.valueOf(authorizationResponse.getRole().getValueDescriptor().getName());
        } catch (Exception e) {
            System.err.println("[Profiler Service] Failed to authorize user with token \"" + token + "\": "
                    + e.getMessage());
            return null;
        }
    }

    public Profile registerProfile(int minAge, int maxAge, String gender, int minHours, int maxHours,
                                List<String> shiftTypes, List<Integer> routeIds) {
        try {
            return this.hyperledgerService.registerProfile(
                    minAge,
                    maxAge,
                    gender,
                    minHours,
                    maxHours,
                    shiftTypes,
                    routeIds
            );
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Profiler Service] Failed to register profile: " + e.getMessage());
            return null;
        }
    }

    public boolean setProfile(String username, String profileId) {
        try {
            this.hyperledgerService.setProfile(username, profileId);
            return true;
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Profiler Service] Failed to set profile " + profileId + " for user " + username + ": "
                    + e.getMessage());
            return false;
        }
    }

    public List<Profile> getProfiles() {
        try {
            return this.hyperledgerService.getProfiles();
        } catch (IOException | ContractException e) {
            System.err.println("[Profiler Service] Failed to get profiles: " + e.getMessage());
            return null;
        }
    }

    public void analyizeDriverData(String username) {
        // TODO: Implement this method
    }
}
