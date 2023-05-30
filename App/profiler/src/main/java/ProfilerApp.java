import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import hmsProto.Auth;
import hmsProto.Profiler;
import models.*;
import services.ProfilerService;
import services.RabbitMqService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProfilerApp {
    public static void main(String[] args) {
        try {
            final String SERVICE_ID = "service_profiler";
            final int NUMBER_OF_THREADS = 1000;

            RabbitMqService rabbitMqService = new RabbitMqService();
            ProfilerService profilerService = new ProfilerService(SERVICE_ID, rabbitMqService);

            Config config = profilerService.loadServiceConfig();
            System.out.println("[Profiler App] Initializing server...");
            profilerService.loadHyperLedgerService(config);

            RpcServer profilerServer = rabbitMqService.newRpcServer(SERVICE_ID);
            Channel channel = profilerServer.getChannel();

            ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            profilerServer.addOperationHandler(Operations.REGISTER_PROFILE, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.RegisterProfileRequest request = Profiler.RegisterProfileRequest
                            .parseFrom(delivery.getBody());

                    UserRole role = profilerService.authorizeUser(request.getToken());
                    Profiler.RegisterProfileResponse.Builder responseBuilder = Profiler.RegisterProfileResponse
                            .newBuilder();

                    if (role.equals(UserRole.SUPERVISOR)) {
                        Profile profile = profilerService.registerProfile(
                                request.getAgeRange().getMin(),
                                request.getAgeRange().getMax(),
                                request.getGender().getValueDescriptor().getName(),
                                request.getShiftHoursRange().getMin(),
                                request.getShiftHoursRange().getMax(),
                                request.getShiftTypesList()
                                        .stream()
                                        .map(t -> t.getValueDescriptor().getName())
                                        .collect(Collectors.toList()),
                                request.getRouteIdsList(),
                                request.getRouteCharacteristicsList()
                                        .stream()
                                        .map(rc -> rc.getValueDescriptor().getName())
                                        .collect(Collectors.toList()),
                                request.getType()
                        );

                        if (profile == null) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage
                                    .newBuilder()
                                    .setDescription("[Profiler Service] Failed to register profile.")
                                    .build()
                            );
                        } else {
                            responseBuilder.setProfileData(Profiler.Profile.newBuilder()
                                    .setId(profile.getId())
                                    .setAgeRange(Profiler.Interval.newBuilder()
                                            .setMin(profile.getMinAge())
                                            .setMax(profile.getMaxAge())
                                            .build()
                                    )
                                    .setGender(Auth.GENDER.valueOf(profile.getGender().toString()))
                                    .setShiftHoursRange(Profiler.Interval.newBuilder()
                                            .setMin(profile.getMinShiftHours())
                                            .setMax(profile.getMaxShiftHours())
                                            .build()
                                    )
                                    .addAllShiftTypes(profile.getShiftTypes()
                                            .stream()
                                            .map(t -> Profiler.SHIFT_TYPE.valueOf(t.toString()))
                                            .collect(Collectors.toList())
                                    )
                                    .addAllRouteIds(profile.getRouteIds())
                            );
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Profiler Service] User does not have permissions to register " +
                                        "new profiles.")
                                .build()
                        );
                    }

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            profilerServer.addOperationHandler(Operations.SET_USER_PROFILE, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.SetProfileRequest request = Profiler.SetProfileRequest.parseFrom(delivery.getBody());

                    UserRole role = profilerService.authorizeUser(request.getToken());
                    Profiler.SetProfileResponse.Builder responseBuilder = Profiler.SetProfileResponse.newBuilder();

                    if (role.equals(UserRole.SUPERVISOR)) {
                        boolean success = profilerService.setProfile(
                                request.getUsername(),
                                request.getProfileId()
                        );

                        if (!success) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                    .setDescription("[Profiler Service] Failed to set profile " +
                                            request.getProfileId() + " to user " + request.getUsername())
                                    .build()
                            );
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Profiler Service] Failed to set profile " +
                                        request.getProfileId() + " to user " + request.getUsername() +
                                        ": Requestor does not have enough privileges.")
                                .build()
                        );
                    }

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            profilerServer.addOperationHandler(Operations.GET_PROFILES, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.GetProfilesRequest request = Profiler.GetProfilesRequest.parseFrom(delivery.getBody());

                    UserRole role = profilerService.authorizeUser(request.getToken());
                    Profiler.GetProfilesResponse.Builder responseBuilder = Profiler.GetProfilesResponse.newBuilder();

                    if (role.equals(UserRole.SUPERVISOR)) {
                        List<Profile> profiles = profilerService.getProfiles();
                        if (profiles == null) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                    .setDescription("[Profiler Service] Failed to get profiles")
                                    .build()
                            );
                        } else {
                            List<Profiler.Profile> protoProfiles = new ArrayList<>();
                            System.out.println(profiles.size());
                            for (Profile profile : profiles) {
                                Profiler.Profile protoProfile = Profiler.Profile.newBuilder()
                                        .setId(profile.getId())
                                        .setAgeRange(Profiler.Interval.newBuilder()
                                                .setMin(profile.getMinAge())
                                                .setMax(profile.getMaxAge())
                                                .build()
                                        )
                                        .setGender(Auth.GENDER.valueOf(profile.getGender().toString()))
                                        .setShiftHoursRange(Profiler.Interval.newBuilder()
                                                .setMin(profile.getMinShiftHours())
                                                .setMax(profile.getMaxShiftHours())
                                                .build()
                                        )
                                        .addAllShiftTypes(profile.getShiftTypes()
                                                .stream()
                                                .map(t -> Profiler.SHIFT_TYPE.valueOf(t.toString()))
                                                .collect(Collectors.toList())
                                        )
                                        .addAllRouteIds(profile.getRouteIds()).build();

                                protoProfiles.add(protoProfile);
                                System.out.println("[Profiler App] Adding profile " +
                                        profile.getId() + " to response list.");
                            }

                            responseBuilder.addAllProfiles(protoProfiles);
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Profiler Service] User is not authorized to view existing profiles.")
                                .build()
                        );
                    }

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            profilerServer.addOperationHandler(Operations.ANALYZE_PROFILE, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.AnalyzeProfileRequest request = Profiler.AnalyzeProfileRequest
                            .parseFrom(delivery.getBody());

                    Profiler.AnalyzeProfileResponse.Builder responseBuilder = Profiler.AnalyzeProfileResponse
                            .newBuilder();

                    try {
                        profilerService.analyizeDriverData(
                                request.getUsername(),
                                request.getLastShiftId(),
                                request.getShiftLogsList()
                                        .stream()
                                        .map(l -> new ShiftLog(
                                                l.getUserId(),
                                                l.getShiftId(),
                                                l.getVehicleId(),
                                                l.getRouteId(),
                                                l.getAverageBPM(),
                                                l.getAverageDrowsiness(),
                                                l.getAverageSpeed(),
                                                l.getTimestamp()
                                        )).collect(Collectors.toList())
                        );
                    } catch(Exception e) {
                        e.printStackTrace();
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription(e.getMessage())
                                .build());
                    }

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                executorService.submit(() -> {
                    try {
                        System.out.println("[Profiler App] Received new operation request!");
                        profilerServer.executeOperationHandler(delivery);
                    } catch (IOException e) {
                        System.err.println("[Profiler App] Unexpected error occurred: " + e.getMessage());
                    }
                });
            };

            channel.basicConsume(SERVICE_ID, false, mainHandler, (consumerTag -> {}));
        } catch (Exception e) {
            System.err.println("[Profiler App] Unexpected error occurred: " + e.getMessage());
        }
    }
}
