package services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import models.Config;
import models.DataLog;
import models.Operations;
import models.RpcClient;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DataService {
    private final RabbitMqService rabbitMqService;
    private final HyperledgerService hyperledgerService;
    private final static String SERVICE_ID = "service_data";

    public DataService(RabbitMqService rabbitMqService) throws Exception {
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

        return new Config(SERVICE_ID, "dataService");
    }

    public DataLog submitUserData(String username, int routeId, int vehicleId, List<Integer> bpmValues,
                               List<Integer> drowsinessValues, List<Integer> speedValues, List<Long> timestampValues) {
        try {
            if (routeId < 0 || vehicleId < 0)
                throw new Exception("RouteId and/or VehicleId not valid: " + routeId + ", " + vehicleId);

            for (int i = 0; i < bpmValues.size(); i++) {
                int bpmValue = bpmValues.get(i);
                int drowsinessValue = drowsinessValues.get(i);
                int speedValue = speedValues.get(i);
                long timestampValue = timestampValues.get(i);

                String exceptionMessage = "";

                if (bpmValue < 0 || bpmValue > 200)
                    exceptionMessage = "Invalid bpm value found: ";
                else if (drowsinessValue < 0 || drowsinessValue > 100)
                    exceptionMessage = "Invalid drowsiness value found: " + drowsinessValue;
                else if (speedValue < 0 || speedValue > 200)
                    exceptionMessage = "Invalid speed value found: " + speedValue;
                else if (timestampValue < 0)
                    exceptionMessage = "Invalid timestamp value found: " + timestampValue;

                if (!exceptionMessage.equals("")) {
                    bpmValues.remove(i);
                    drowsinessValues.remove(i);
                    speedValues.remove(i);
                    timestampValues.remove(i);
                    throw new Exception(exceptionMessage);
                }
            }

            return this.hyperledgerService.submitUserData(
                    username,
                    routeId,
                    vehicleId,
                    bpmValues,
                    drowsinessValues,
                    speedValues,
                    timestampValues
            );
        } catch (Exception e) {
            System.err.println("[Data Service] Failed to submit data logs for user" + username + ": " + e.getMessage());
            return null;
        }
    }

    public List<DataLog> getDataLogsForUser(String username) {
        try {
            return this.hyperledgerService.getLogsForUser(username);
        } catch (IOException | ContractException e) {
            System.err.println("[Data Service] Failed to submit data logs for user" + username + ": " + e.getMessage());
            return null;
        }
    }
}
