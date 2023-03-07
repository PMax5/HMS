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
import java.util.stream.Collectors;

public class ProfilerApp {
    public static void main(String[] args) {
        try {
            RabbitMqService rabbitMqService = new RabbitMqService();
            ProfilerService profilerService = new ProfilerService(rabbitMqService);

            Config config = profilerService.loadServiceConfig();
            System.out.println("[Profiler App] Initializing server...");

            RpcServer profilerServer = rabbitMqService.newRpcServer(config.getChannelName());
            Channel channel = profilerServer.getChannel();

            profilerServer.addOperationHandler(Operations.REGISTER_PROFILE, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.RegisterProfileRequest request = Profiler.RegisterProfileRequest
                            .parseFrom(delivery.getBody());

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
                            request.getRouteIdsList()
                    );

                    Profiler.RegisterProfileResponse.Builder responseBuilder = Profiler.RegisterProfileResponse
                            .newBuilder();

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

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            profilerServer.addOperationHandler(Operations.SET_USER_PROFILE, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Profiler.SetProfileRequest request = Profiler.SetProfileRequest.parseFrom(delivery.getBody());

                    boolean success = profilerService.setProfile(
                            request.getUsername(),
                            request.getProfileId()
                    );

                    Profiler.SetProfileResponse.Builder responseBuilder = Profiler.SetProfileResponse.newBuilder();

                    if (!success) {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Profiler Service] Failed to set profile " +
                                        request.getProfileId() + " to user " + request.getUsername())
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

                    List<Profile> profiles = profilerService.getProfiles();

                    Profiler.GetProfilesResponse.Builder responseBuilder = Profiler.GetProfilesResponse.newBuilder();

                    if (profiles == null) {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Profiler Service] Failed to get profiles")
                                .build()
                        );
                    } else {
                        List<Profiler.Profile> protoProfiles = new ArrayList<>();
                        for (Profile p: profiles) {
                            Profiler.Profile protoProfile = Profiler.Profile.newBuilder()
                                    .setId(p.getId())
                                    .setAgeRange(Profiler.Interval.newBuilder()
                                            .setMin(p.getMinAge())
                                            .setMax(p.getMaxAge())
                                            .build()
                                    )
                                    .setGender(Auth.GENDER.valueOf(p.getGender().toString()))
                                    .setShiftHoursRange(Profiler.Interval.newBuilder()
                                            .setMin(p.getMinShiftHours())
                                            .setMax(p.getMaxShiftHours())
                                            .build()
                                    )
                                    .addAllShiftTypes(p.getShiftTypes()
                                            .stream()
                                            .map(t -> Profiler.SHIFT_TYPE.valueOf(t.toString()))
                                            .collect(Collectors.toList())
                                    )
                                    .addAllRouteIds(p.getRouteIds())
                                    .build();

                            protoProfiles.add(protoProfile);
                        }

                        responseBuilder.addAllProfiles(protoProfiles);
                    }

                    profilerServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                System.out.println("[Profiler App] Received new operation request!");
                profilerServer.executeOperationHandler(delivery);
            };
            
            channel.basicConsume(config.getChannelName(), false, mainHandler, (consumerTag -> {}));
        } catch (Exception e) {
            System.err.println("[Profiler App] Unexpected error occurred: " + e.getMessage());
        }
    }
}
