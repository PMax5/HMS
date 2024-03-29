package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import repos.RoutesRepo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProfilerService {
    private final RoutesRepo routesRepo;
    private final Map<Integer, Route> routesCache;
    private HyperledgerService hyperledgerService;
    private RabbitMqService rabbitMqService;
    private String serviceId;
    private Config config;
    private List<Profile> profiles;

    public ProfilerService(Config config) {
        this.config = config;
        this.routesRepo = new RoutesRepo(config);
        this.profiles = new ArrayList<>();
        this.routesCache = new TreeMap<>();
    }

    public ProfilerService(String serviceId, RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
        this.routesRepo = new RoutesRepo(null);
        this.serviceId = serviceId;
        this.profiles = new ArrayList<>();
        this.routesCache = new TreeMap<>();
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel));

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
            System.err.println("[Profiler Service] Failed to authorize user with token \"" + token + "\": "
                    + e.getMessage());
            return null;
        }
    }

    public Profile registerProfile(int minAge, int maxAge, String gender, int minHours, int maxHours,
                                List<String> shiftTypes, List<Integer> routeIds, List<String> routeCharacteristics,
                                   int type) {
        try {

            if (minAge < 18 || maxAge < 0 || maxAge >= 67 || minAge >= maxAge || gender == null || minHours < 0 ||
                    maxHours < 0 || maxHours > 24 || minHours >= maxHours || shiftTypes == null ||
                    routeIds == null || routeCharacteristics == null) {
                throw new Exception("One of the profile fields is incorrect.");
            }

            for (int routeId: routeIds) {
                this.routesRepo.getRoute(routeId);
            }

            if (this.profiles.size() <= 0) {
                this.profiles = this.getProfiles();
            }

            Profile profile = this.hyperledgerService.registerProfile(
                    minAge,
                    maxAge,
                    gender,
                    minHours,
                    maxHours,
                    shiftTypes,
                    routeIds,
                    routeCharacteristics,
                    type
            );

            if (profile != null) {
                this.profiles.add(profile);
                System.out.println("[Profiler Service] Successfully registered new profile: " + profile.getId());
            }

            return profile;
        } catch (Exception e) {
            System.err.println("[Profiler Service] Failed to register profile: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteProfileById(String profileId) {
        try {
            this.hyperledgerService.deleteProfileById(profileId);
            this.profiles.removeIf(profile -> profile.getId().equals(profileId));
            return true;
        } catch(Exception e) {
            System.err.println("[Profiler Service] Failed to delete profile: " + e.getMessage());
            return false;
        }
    }

    private boolean profileExists(String profileId) {
        for (Profile profile: this.profiles) {
            if (profile.getId().equals(profileId)) {
                return true;
            }
        }

        return false;
    }

    public boolean setProfile(String username, String profileId) {
        try {
            if (!this.profileExists(profileId)) {
                return false;
            }

            this.hyperledgerService.setProfile(username, profileId);
            Profile newProfile = null;
            for (Profile profile: this.profiles) {
                if (profile.getId().equals(profileId)) {
                    newProfile = profile;
                }
            }

            System.out.println("[Profiler Service] User " + username + " profile was revised to profile with ID: " +
                    profileId);
            if (newProfile != null) {
                System.out.println(newProfile);
            }
            return true;
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Profiler Service] Failed to set profile " + profileId + " for user " + username + ": "
                    + e.getMessage());

            Throwable cause = e.getCause();
            if (cause instanceof TransactionEventException) {
                BlockEvent.TransactionEvent txEvent = ((TransactionEventException) cause).getTransactionEvent();
                byte validationCode = txEvent.getValidationCode();
                System.err.println(String.valueOf(validationCode));
            }
            return false;
        }
    }

    public List<Profile> getProfiles() {
        try {
            if (this.profiles.size() <= 0) {
                this.profiles = this.hyperledgerService.getProfiles();
            }

            System.out.println("[Profiler Service] Fetching all profiles.");
            return this.profiles;
        } catch (IOException | ContractException e) {
            System.err.println("[Profiler Service] Failed to get profiles: " + e.getMessage());
            return null;
        }
    }

    public Profile getUserProfile(String username, List<Profile> profiles) throws IOException, ContractException,
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

    public void analyizeDriverData(String username, String lastShiftId, List<ShiftLog> shiftLogs) throws Exception {
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
                if(!this.routesCache.containsKey(routeId)) {
                    this.routesCache.put(routeId, this.routesRepo.getRoute(routeId));
                }
                Route route = this.routesCache.get(routeId);
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
                System.out.println("[Profiler Service] Analyzing profile for user " + username + " with \n"
                        + userProfile);
                if (userProfile != null) {
                    if (lastShiftLog.getAverageBPM() > this.config.getMaxHealthyBPM() &&
                            problematicShiftsBPM.size() > 1) {
                        for (ShiftType shiftType: userProfile.getShiftTypes()) {
                            if (shiftType.equals(ShiftType.SHIFT_MORNING) ||
                                    shiftType.equals(ShiftType.SHIFT_AFTERNOON)) {
                                userProfile = this.getProfileForShiftType(profiles, ShiftType.SHIFT_NIGHT);
                                if (userProfile != null) {
                                    this.setProfile(username, userProfile.getId());
                                } else {
                                    throw new Exception("Profile is null when trying to change shift (BPM).");
                                }
                                return;
                            }
                        }

                        switch (this.getMostFrequentRouteCharacteristic(characteristicsFrequencyBPM)) {
                            case HIGH_TRAFFIC -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.LOW_TRAFFIC
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                            case THIN_ROADS -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.LARGE_ROADS
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                            case CRIMINAL_AREA -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.REGULAR_AREA
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                        }
                    } else if (lastShiftLog.getAverageDrowsiness() > this.config.getMaxDrowsiness() &&
                            problematicShiftsDrowsiness.size() > 1) {
                        for (ShiftType shiftType: userProfile.getShiftTypes()) {
                            if (shiftType.equals(ShiftType.SHIFT_NIGHT)) {
                                userProfile = this.getProfileForShiftType(profiles, ShiftType.SHIFT_MORNING);
                                if (userProfile != null) {
                                    return;
                                } else {
                                    throw new Exception("Profile is null when trying to change shift (Drowsiness).");
                                }
                            }
                        }

                        switch (this.getMostFrequentRouteCharacteristic(characteristicsFrequencyDrowsiness)) {
                            case LOW_TRAFFIC -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.HIGH_TRAFFIC
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                            case LARGE_ROADS -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.THIN_ROADS
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                            case REGULAR_AREA -> {
                                final Profile profile = this.getProfileForRouteCharacteristic(
                                        userProfile,
                                        profiles,
                                        RouteCharacteristic.CRIMINAL_AREA
                                );

                                userProfile = profile != null ? profile : userProfile;
                            }
                        }
                    }

                    this.setProfile(username, userProfile.getId());
                } else {
                    throw new Exception("[Profiler Service] Wasn't able to fetch current user profile.");
                }
            } else {
                throw new Exception("[Profiler Service] There is no shift with the specified last shift id.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("[Profiler Service] Failed to get routes for one of the driver's shifts: " + e.getMessage());
        }
    }
}
