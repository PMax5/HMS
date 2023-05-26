package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;
import repos.Route;
import repos.RoutesRepo;
import repos.Vehicle;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DataService {
    private final Map<String, Shift> activeShifts;
    private final Map<Integer, Route> routesCache;
    private final RoutesRepo routesRepo;
    private RabbitMqService rabbitMqService;
    private String serviceId;
    private HyperledgerService hyperledgerService;

    public DataService(Config config) {
        this.routesRepo = new RoutesRepo(config);
        this.activeShifts = new HashMap<>();
        this.routesCache = new HashMap<>();
    }

    public DataService(String serviceId, RabbitMqService rabbitMqService) {
        this.serviceId = serviceId;
        this.rabbitMqService = rabbitMqService;
        this.routesRepo = new RoutesRepo(null);
        this.activeShifts = new HashMap<>();
        this.routesCache = new HashMap<>();
    }

    public void loadHyperledgerService(Config config) throws Exception {
        this.hyperledgerService = new HyperledgerService(config);
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = this.rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel));

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
            RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel));

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

    public void removeActiveShift(String username) {
        this.activeShifts.remove(username);
    }

    public String startShift(String username, int routeId, int vehicleId) {
        if (this.activeShifts.containsKey(username)) {
            System.err.println("[Data Service] User " + username + " tried to start a shift that already started.");
            return null;
        } else if (routeId < 0 || vehicleId < 0 || username.isEmpty()) {
            System.err.println("[Data Service] User " + username + " tried to start a shift for an " +
                    "invalid route/vehicle.");
            return null;
        }

        try {
            if (!this.routesCache.containsKey(routeId)) {
                this.routesCache.put(routeId, this.routesRepo.getRoute(routeId));
            }
            AtomicReference<Vehicle> vehicle = new AtomicReference<>();
            Route route = this.routesCache.get(routeId);
            route.getVehicles().forEach(v -> {
                if (v.getId() == vehicleId) {
                    vehicle.set(v);
                }
            });

            if (vehicle.get() == null) {
                System.err.println("[Data Service] User " + username + " tried to start a shift for an " +
                        "invalid vehicle.");
                return null;
            }

            String shiftId = UUID.randomUUID().toString();
            this.activeShifts.put(username, new Shift(
                    username,
                    shiftId,
                    routeId,
                    vehicleId
            ));

            return shiftId;
        } catch (Exception e) {
            System.err.println("[Data Service] Failed to retrieve route or vehicle: " + e.getMessage());
            return null;
        }

    }

    public ShiftLog endShift(String username) {
        if (!this.activeShifts.containsKey(username)) {
            System.err.println("[Data Service] User " + username + " tried to end a shift that does not exist.");
            return null;
        }

        ShiftLog shiftLog = this.processShiftData(this.activeShifts.get(username));
        this.activeShifts.remove(username);
        if (shiftLog != null) {
            this.routesCache.remove(shiftLog.getRouteId());
        }
        System.out.println("[Data Service] Ended shift for user: " + username);
        return shiftLog;
    }

    private ShiftLog processShiftData(Shift shift) {
        try {
            List<DataLog> dataLogs = this.hyperledgerService.getLogsForShift(shift.getShiftId());

            AtomicInteger totalBpmValues = new AtomicInteger();
            AtomicInteger totalDrowsinessValues = new AtomicInteger();
            AtomicInteger totalSpeedValues = new AtomicInteger();

            AtomicInteger totalBpm = new AtomicInteger();
            AtomicInteger totalDrowsiness = new AtomicInteger();
            AtomicInteger totalSpeed = new AtomicInteger();

            dataLogs.forEach(log -> {
                log.getBpmValues().forEach(totalBpm::addAndGet);
                log.getDrowsinessValues().forEach(totalDrowsiness::addAndGet);
                log.getSpeedValues().forEach(totalSpeed::addAndGet);

                totalBpmValues.addAndGet(log.getBpmValues().size());
                totalDrowsinessValues.addAndGet(log.getDrowsinessValues().size());
                totalSpeedValues.addAndGet(log.getSpeedValues().size());

            });

            if (totalBpmValues.get() == 0 || totalDrowsinessValues.get() == 0 || totalSpeedValues.get() == 0) {
                throw new Exception("One of the VBS lists is empty.");
            }

            return this.hyperledgerService.submitShiftLogData(
                    shift.getUserId(),
                    shift.getShiftId(),
                    shift.getVehicleId(),
                    shift.getRouteId(),
                    totalBpm.get() / totalBpmValues.get(),
                    totalDrowsiness.get() / totalDrowsinessValues.get(),
                    totalSpeed.get() / totalSpeedValues.get()
            );
        } catch (Exception e) {
            System.err.println("[Data Service] Failed to process data logs for user " + shift.getUserId() +
                    ": " + e.getMessage());
            return null;
        }
    }

    public List<ShiftLog> getShiftLogsForUser(String userId) {
        try {
            System.out.println("HERE!");
            return this.hyperledgerService.getShiftLogsForUser(userId);
        } catch (ContractException | IOException e) {
            System.err.println("[Data Service] Failed to get data logs for user " + userId + ": " + e.getMessage());
            return null;
        }
    }

    public DataLog submitUserData(String username, int routeId, int vehicleId, List<Integer> bpmValues,
                               List<Integer> drowsinessValues, List<Integer> speedValues, List<Long> timestampValues) {
        try {
            if (routeId < 0 || vehicleId < 0)
                throw new Exception("RouteId and/or VehicleId not valid: " + routeId + ", " + vehicleId);
            else if (!this.activeShifts.containsKey(username))
                throw new Exception("No active shift was found for user: " + username);

            if (!this.routesCache.containsKey(routeId)) {
                this.routesCache.put(routeId, this.routesRepo.getRoute(routeId));
            }

            Route route = this.routesCache.get(routeId);
            AtomicReference<Vehicle> vehicle = new AtomicReference<>();
            route.getVehicles().forEach(v -> {
                if (v.getId() == vehicleId) {
                    vehicle.set(v);
                }
            });

            if (vehicle.get() == null) {
                throw new Exception("[Data Service] User " + username + " tried to start a shift for an " +
                        "invalid vehicle.");
            }

            List<Integer> finalBpmValues = new ArrayList<>();
            List<Integer> finalDrowsinessValues = new ArrayList<>();
            List<Integer> finalSpeedValues = new ArrayList<>();
            List<Long> finalTimestampValues = new ArrayList<>();

            for (int i = 0; i < bpmValues.size(); i++) {
                int bpmValue = bpmValues.get(i);
                int drowsinessValue = drowsinessValues.get(i);
                int speedValue = speedValues.get(i);
                long timestampValue = timestampValues.get(i);

                String exceptionMessage = "";

                if (bpmValue < 0 || bpmValue > 200)
                    exceptionMessage = "Invalid bpm value found: " + bpmValue;
                else if (drowsinessValue < 0 || drowsinessValue > 100)
                    exceptionMessage = "Invalid drowsiness value found: " + drowsinessValue;
                else if (speedValue < 0 || speedValue > 200)
                    exceptionMessage = "Invalid speed value found: " + speedValue;
                else if (timestampValue < 0)
                    exceptionMessage = "Invalid timestamp value found: " + timestampValue;

                if (!exceptionMessage.equals("")) {
                    System.out.println("[Data Service] " + exceptionMessage);
                } else {
                    finalBpmValues.add(bpmValue);
                    finalDrowsinessValues.add(drowsinessValue);
                    finalSpeedValues.add(speedValue);
                    finalTimestampValues.add(timestampValue);
                }
            }

            return this.hyperledgerService.submitUserData(
                    username,
                    routeId,
                    vehicleId,
                    finalBpmValues,
                    finalDrowsinessValues,
                    finalSpeedValues,
                    finalTimestampValues,
                    this.activeShifts.get(username).getShiftId()
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
