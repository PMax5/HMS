package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DataService {
    private final RabbitMqService rabbitMqService;
    private final String serviceId;
    private HyperledgerService hyperledgerService;
    private Map<String, String> activeShifts;

    public DataService(String serviceId, RabbitMqService rabbitMqService) {
        this.serviceId = serviceId;
        this.rabbitMqService = rabbitMqService;
        this.activeShifts = new HashMap<>();
    }

    public void loadHyperledgerService(Config config) throws Exception {
        this.hyperledgerService = new HyperledgerService(config);
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = this.rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel), configQueueName);

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(serviceId)
                .build();

        final byte[] response = rpcClient.sendRequest(
                configQueueName,
                channel,
                Operations.CONFIG_REQUEST,
                configRequest.toByteArray()
        );
        rpcClient.close();

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        return new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
    }

    public UserRole authorizeUser(String token) {
        try {
            final String registryQueueName = "service_registry";
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
            System.err.println("[Data Service] Failed to authorize user with token \"" + token + "\": "
                    + e.getMessage());
            return null;
        }
    }

    public String startShift(String username) {
        if (this.activeShifts.containsKey(username)) {
            System.err.println("[Data Service] User " + username + " tried to start a shift that already started.");
            return null;
        }

        String shiftId = UUID.randomUUID().toString();
        this.activeShifts.put(username, shiftId);

        return shiftId;
    }

    public boolean endShift(String username) {
        if (!this.activeShifts.containsKey(username)) {
            System.err.println("[Data Service] User " + username + " tried to end a shift that does not exist.");
            return false;
        }

        this.activeShifts.remove(username);
        return true;
    }

    private void processShiftData(String shiftId) {
        // TODO: Calculate average BPM and drowsiness values.
    }


    public DataLog submitUserData(String username, int routeId, int vehicleId, List<Integer> bpmValues,
                               List<Integer> drowsinessValues, List<Integer> speedValues, List<Long> timestampValues) {
        try {
            if (routeId < 0 || vehicleId < 0)
                throw new Exception("RouteId and/or VehicleId not valid: " + routeId + ", " + vehicleId);
            else if (!this.activeShifts.containsKey(username))
                throw new Exception("No active shift was found for user: " + username);

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
                    timestampValues,
                    this.activeShifts.get(username)
            );
        } catch (Exception e) {
            System.err.println("[Data Service] Failed to submit data logs for user " + username + ": " + e.getMessage());
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
