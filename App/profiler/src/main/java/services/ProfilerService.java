package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ProfilerService {
    private final RabbitMqService rabbitMqService;
    private final HyperledgerService hyperledgerService;
    private final static String SERVICE_ID = "service_profiler";

    public ProfilerService(RabbitMqService rabbitMqService) throws Exception {
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
        rpcClient.close();

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        System.out.println(configResponse.getServiceConfig());
        return new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
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
