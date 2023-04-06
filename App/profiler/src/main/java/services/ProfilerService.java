package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;
import repos.RoutesRepo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProfilerService {
    private final RabbitMqService rabbitMqService;
    private final RoutesRepo routesRepo;
    private final String serviceId;
    private HyperledgerService hyperledgerService;
    private Config config;

    public ProfilerService(String serviceId, RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
        this.routesRepo = new RoutesRepo();
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
        this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
        return this.config;
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
                                List<String> shiftTypes, List<Integer> routeIds, List<String> routeCharacteristics) {
        try {
            return this.hyperledgerService.registerProfile(
                    minAge,
                    maxAge,
                    gender,
                    minHours,
                    maxHours,
                    shiftTypes,
                    routeIds,
                    routeCharacteristics
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

    private Profile getUserProfile(String username, List<Profile> profiles) throws IOException, ContractException,
            InterruptedException, TimeoutException {
        String profileId = this.hyperledgerService.getUserProfile(username);
        for (Profile profile: profiles) {
            if (profile.getId().equals(profileId)) {
                return profile;
            }
        }

        return null;
    }

    private Profile getProfileForRouteCharacteristic(
            Profile oldProfile, List<Profile> profiles, RouteCharacteristic routeCharacteristic) {
        for (Profile profile: profiles) {
            if (profile.getId().equals(oldProfile.getId()))
                continue;

            if (profile.getRouteCharacteristics().contains(routeCharacteristic))
                return profile;
        }

        return null;
    }

    private Profile getProfileForShiftType(List<Profile> profiles, ShiftType shiftType) {
        for (Profile profile: profiles) {
            if (profile.getShiftTypes().contains(shiftType))
                return profile;
        }

        return null;
    }

    private RouteCharacteristic getMostFrequentRouteCharacteristic(
            Map<RouteCharacteristic, Integer> characteristicsFrequency) {
        AtomicReference<RouteCharacteristic> mostFrequentCharacteristic = new AtomicReference<>();
        AtomicInteger maxFrequency = new AtomicInteger();
        characteristicsFrequency.forEach((key, value) -> {
            if (maxFrequency.get() == 0) {
                maxFrequency.set(value);
                mostFrequentCharacteristic.set(key);
            } else if (maxFrequency.get() < value) {
                maxFrequency.set(value);
                mostFrequentCharacteristic.set(key);
            }
        });

        return mostFrequentCharacteristic.get();
    }

    public void analyizeDriverData(String username, String lastShiftId, List<ShiftLog> shiftLogs) {
        ShiftLog lastShiftLog = null;
        List<ShiftLog> problematicShiftsBPM = new ArrayList<>();
        List<ShiftLog> problematicShiftsDrowsiness = new ArrayList<>();
        Map<RouteCharacteristic, Integer> characteristicsFrequencyBPM = new HashMap<>();
        Map<RouteCharacteristic, Integer> characteristicsFrequencyDrowsiness = new HashMap<>();
        List<Profile> profiles = this.getProfiles();

        try {
            for (ShiftLog shiftLog : shiftLogs) {
                if (shiftLog.getShiftId().equals(lastShiftId)) {
                    lastShiftLog = shiftLog;
                }

                final int routeId = shiftLog.getRouteId();
                Route route = this.routesRepo.getRoute(routeId);
                if (shiftLog.getAverageBPM() > this.config.getMaxHealthyBPM()) {
                    problematicShiftsBPM.add(shiftLog);
                    route.getCharacteristics().forEach(routeCharacteristic -> {
                        if (!characteristicsFrequencyBPM.containsKey(routeCharacteristic)) {
                            characteristicsFrequencyBPM.put(routeCharacteristic, 1);
                        } else {
                            characteristicsFrequencyBPM.put(routeCharacteristic,
                                    characteristicsFrequencyBPM.get(routeCharacteristic) + 1);
                        }
                    });
                } else if (shiftLog.getAverageDrowsiness() > this.config.getMaxDrowsiness()) {
                    route.getCharacteristics().forEach(routeCharacteristic -> {
                        if (!characteristicsFrequencyDrowsiness.containsKey(routeCharacteristic)) {
                            characteristicsFrequencyDrowsiness.put(routeCharacteristic, 1);
                        } else {
                            characteristicsFrequencyDrowsiness.put(routeCharacteristic,
                                    characteristicsFrequencyDrowsiness.get(routeCharacteristic) + 1);
                        }
                    });
                    problematicShiftsDrowsiness.add(shiftLog);
                }
            }

            if (lastShiftLog != null) {
                Profile userProfile = this.getUserProfile(username, profiles);
                if (userProfile != null) {
                    if (lastShiftLog.getAverageBPM() > this.config.getMaxHealthyBPM() &&
                            problematicShiftsBPM.size() > this.config.getMaxProblematicShifts()) {
                        for (ShiftType shiftType: userProfile.getShiftTypes()) {
                            if (shiftType.equals(ShiftType.SHIFT_MORNING) ||
                                    shiftType.equals(ShiftType.SHIFT_AFTERNOON)) {
                                userProfile = this.getProfileForShiftType(profiles, ShiftType.SHIFT_NIGHT);
                                if (userProfile != null) {
                                    this.setProfile(username, userProfile.getId());
                                } else {
                                    System.err.println("Profile is null when trying to change shift (BPM).");
                                }
                                return;
                            }
                        }

                        switch (this.getMostFrequentRouteCharacteristic(characteristicsFrequencyBPM)) {
                            case HIGH_TRAFFIC -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.LOW_TRAFFIC
                            );
                            case THIN_ROADS -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.LARGE_ROADS
                            );
                            case CRIMINAL_AREA -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.REGULAR_AREA
                            );
                        }
                    } else if (lastShiftLog.getAverageDrowsiness() > this.config.getMaxDrowsiness() &&
                            problematicShiftsDrowsiness.size() > this.config.getMaxProblematicShifts()) {
                        for (ShiftType shiftType: userProfile.getShiftTypes()) {
                            if (shiftType.equals(ShiftType.SHIFT_NIGHT)) {
                                userProfile = this.getProfileForShiftType(profiles, ShiftType.SHIFT_MORNING);
                                if (userProfile != null) {
                                    this.setProfile(username, userProfile.getId());
                                } else {
                                    System.err.println("Profile is null when trying to change shift (Drowsiness).");
                                }
                                return;
                            }
                        }

                        switch (this.getMostFrequentRouteCharacteristic(characteristicsFrequencyDrowsiness)) {
                            case LOW_TRAFFIC -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.HIGH_TRAFFIC
                            );
                            case LARGE_ROADS -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.THIN_ROADS

                            );
                            case REGULAR_AREA -> userProfile = this.getProfileForRouteCharacteristic(
                                    userProfile,
                                    profiles,
                                    RouteCharacteristic.CRIMINAL_AREA
                            );
                        }
                    }

                    if (userProfile != null) {
                        this.setProfile(username, userProfile.getId());
                    } else {
                        System.err.println("[Profiler Service] Wasn't able to change driver profile.");
                    }
                } else {
                    System.err.println("[Profiler Service] Wasn't able to fetch current user profile.");
                }
            } else {
                System.err.println("[Profiler Service] There is no shift with the specified last shift id.");
            }
        } catch (Exception e) {
            System.err.println("[Profiler Service] Failed to get routes for one of the driver's shifts.");
        }
    }
}
