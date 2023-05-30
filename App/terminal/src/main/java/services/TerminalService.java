package services;

import hmsProto.Auth;
import hmsProto.GatewaysGrpc;
import hmsProto.Profiler;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class TerminalService implements AutoCloseable {

    private final GatewaysGrpc.GatewaysBlockingStub stub;
    private final ManagedChannel channel;
    private String userToken;
    private String username;

    public TerminalService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = GatewaysGrpc.newBlockingStub(this.channel);
    }

    public void loginUser(String username, String password) throws Exception {
        Auth.UserAuthenticationRequest authenticationRequest = Auth.UserAuthenticationRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        System.out.println("[Terminal Service] Logging in user: " + username);
        Auth.UserAuthenticationResponse authenticationResponse = this.stub.authenticateUser(authenticationRequest);
        if (authenticationResponse.hasErrorMessage()) {
            throw new Exception(authenticationResponse.getErrorMessage().getDescription());
        }


        this.userToken = authenticationResponse.getToken();
        this.username = username;
    }

    public void logoutUser() throws Exception {
        if (this.userToken == null) {
            return;
        }

        Auth.UserLogoutRequest logoutRequest = Auth.UserLogoutRequest.newBuilder()
                .setToken(this.userToken)
                .build();

        System.out.println("[Terminal Service] Logging out user: " + this.username);
        Auth.UserLogoutResponse logoutResponse = this.stub.logoutUser(logoutRequest);
        if (logoutResponse.hasErrorMessage()) {
            throw new Exception(logoutResponse.getErrorMessage().getDescription());
        }

        this.userToken = null;
        this.username = null;
    }

    public void registerUser(String username, String name, int age, String gender, String role, String password)
            throws Exception {
        if (this.userToken == null) {
            throw new Exception("[Terminal Service] Failed to login user, shutting down.");
        }

        Auth.UserRegistrationRequest userRegistrationRequest = Auth.UserRegistrationRequest.newBuilder()
                .setUsername(username)
                .setName(name)
                .setAge(age)
                .setGender(Auth.GENDER.valueOf(gender))
                .setRole(Auth.ROLE.valueOf(role))
                .setPassword(password)
                .setToken(this.userToken)
                .build();

        System.out.println("[Terminal Service] Registering user with username " + username);
        Auth.UserRegistrationResponse userRegistrationResponse = this.stub.registerUser(userRegistrationRequest);

        if (userRegistrationResponse.hasErrorMessage()) {
            throw new Exception(userRegistrationResponse.getErrorMessage().getDescription());
        }
    }

    public void registerProfile(int minAge, int maxAge, String gender, int minHours, int maxHours,
                                List<String> shiftTypes, List<Integer> routeIds,
                                List<String> routeCharacteristics, int type) throws Exception {
        System.out.println("[Terminal Service] Preparing new profile request.");
        Profiler.RegisterProfileRequest registerProfileRequest = Profiler.RegisterProfileRequest.newBuilder()
                .setAgeRange(Profiler.Interval.newBuilder()
                        .setMin(minAge)
                        .setMax(maxAge)
                        .build())
                .setGender(Auth.GENDER.valueOf(gender))
                .setShiftHoursRange(Profiler.Interval.newBuilder()
                        .setMin(minHours)
                        .setMax(maxHours)
                        .build())
                .addAllShiftTypes(shiftTypes.stream().map(Profiler.SHIFT_TYPE::valueOf).collect(Collectors.toList()))
                .addAllRouteIds(routeIds)
                .addAllRouteCharacteristics(routeCharacteristics.stream().map(Profiler.ROUTE_CHARACTERISTIC::valueOf)
                        .collect(Collectors.toList()))
                .setType(type)
                .setToken(this.userToken)
                .build();

        System.out.println("[Terminal Service] Registering new profile.");
        Profiler.RegisterProfileResponse registerProfileResponse = this.stub.registerProfile(registerProfileRequest);

        if (registerProfileResponse.hasErrorMessage()) {
            throw new Exception(registerProfileResponse.getErrorMessage().getDescription());
        }
    }

    public void getProfiles() throws Exception {
        Profiler.GetProfilesRequest getProfilesRequest = Profiler.GetProfilesRequest.newBuilder()
                .setToken(this.userToken)
                .build();

        System.out.println("[Terminal Service] Fetching profiles for driver " + this.username);
        Profiler.GetProfilesResponse getProfilesResponse = this.stub.getProfiles(getProfilesRequest);

        if (getProfilesResponse.hasErrorMessage()) {
            throw new Exception(getProfilesResponse.getErrorMessage().getDescription());
        }

        getProfilesResponse.getProfilesList().forEach(p -> {
            System.out.println("PROFILE " + p.getId() +
                    "\n Min Age: " + p.getAgeRange().getMin() +
                    "\n Max Age: " + p.getAgeRange().getMax() +
                    "\n Gender: " + p.getGender() +
                    "\n Min Shift Hours: " + p.getShiftHoursRange().getMin() +
                    "\n Max Shift Hours: " + p.getShiftHoursRange().getMax() +
                    "\n Shift Types: " + p.getShiftTypesList() +
                    "\n Route IDs: " + p.getRouteIdsList() +
                    "\n Route Characteristics: " + p.getRouteCharacteristicsList() +
                    "\n Type: " + p.getType() + "\n ================================ \n"
                    );
        });
    }

    public void setUserProfile(String username, String profileId) throws Exception {
        Profiler.SetProfileRequest setProfileRequest = Profiler.SetProfileRequest.newBuilder()
                .setUsername(username)
                .setProfileId(profileId)
                .setToken(this.userToken)
                .build();

        System.out.println("[Terminal Service] Setting a new profile for user " + username + ": " + profileId);
        Profiler.SetProfileResponse setProfileResponse = this.stub.setUserProfile(setProfileRequest);

        if (setProfileResponse.hasErrorMessage()) {
            throw new Exception(setProfileResponse.getErrorMessage().getDescription());
        }
    }

    @Override
    public void close() {
        this.channel.shutdown();
    }
}
