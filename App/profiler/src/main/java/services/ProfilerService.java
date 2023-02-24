package services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import models.*;

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

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);

        // TODO: Fetch config from service.
        // this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);

        return new Config(SERVICE_ID, "profilerUser");
    }

    public void registerProfile(int id, int minAge, int maxAge, Gender gender, int minHours, int maxHours,
                                List<ShiftType> shiftTypes, List<Integer> routeIds) {
        // TODO: Implement this method
    }

    public void setProfile(String username, int profileId) {
        // TODO: Implement this method
    }

    public void analyizeDriverData(String username) {
        // TODO: Implement this method
    }
}
