package services;

import exceptions.OBUException;
import hmsProto.Auth;
import hmsProto.Data;
import hmsProto.GatewaysGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OBUService implements AutoCloseable {

    private final GatewaysGrpc.GatewaysBlockingStub blockingStub;
    private final GatewaysGrpc.GatewaysStub asyncStub;
    private final ManagedChannel channel;
    private final List<Integer> bpm;
    private final List<Integer> drowsiness;
    private final List<Integer> speeds;
    private final List<Long> timestamps;
    private String userToken;
    private String username;
    private String shiftId;
    private int routeId;
    private int vehicleId;

    public OBUService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = GatewaysGrpc.newBlockingStub(this.channel);
        this.asyncStub = GatewaysGrpc.newStub(this.channel);
        this.bpm = new ArrayList<>();
        this.drowsiness = new ArrayList<>();
        this.speeds = new ArrayList<>();
        this.timestamps = new ArrayList<>();
    }

    public void loginUser(String username, String password) throws OBUException {
        Auth.UserAuthenticationRequest authenticationRequest = Auth.UserAuthenticationRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        System.out.println("[OBU Service] Logging in user: " + username);
        Auth.UserAuthenticationResponse authenticationResponse = this.blockingStub.authenticateUser(authenticationRequest);
        if (authenticationResponse.hasErrorMessage()) {
            throw new OBUException(authenticationResponse.getErrorMessage().getDescription());
        }


        this.userToken = authenticationResponse.getToken();
        this.username = username;
    }

    public void bulkLoginUser(String username, String password, int iterations) throws OBUException {
        long startTime = Instant.now().getEpochSecond();

        final int[] counter = {0};
        for (int i = 0; i < iterations; i++) {
            Auth.UserAuthenticationRequest authenticationRequest = Auth.UserAuthenticationRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();

            System.out.println("[OBU Service] Logging in user: " + username);

            int finalI = i;
            this.asyncStub.authenticateUser(authenticationRequest, new StreamObserver<Auth.UserAuthenticationResponse>() {
                @Override
                public void onNext(Auth.UserAuthenticationResponse userAuthenticationResponse) {
                    if (userAuthenticationResponse.hasErrorMessage()) {
                        System.err.println("[OBU Service] " + userAuthenticationResponse.getErrorMessage());
                    }
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    if (counter[0] == iterations - 1) {
                        long endTime = Instant.now().getEpochSecond();
                        float throughput = (float) iterations / (endTime - startTime);
                        System.out.println("[OBU Service] Time: " +
                                TimeUnit.SECONDS.toSeconds(endTime - startTime));
                        System.out.println("[OBU Service] Service throughput: " + throughput);
                    } else {
                        counter[0] = counter[0] + 1;
                    }

                    System.out.println("[OBU Service] Response from API Gateways for iteration " + finalI);
                }
            });
        }
    }

    public void logoutUser() throws OBUException {
        if (this.userToken == null) {
            return;
        }

        Auth.UserLogoutRequest logoutRequest = Auth.UserLogoutRequest.newBuilder()
                .setToken(this.userToken)
                .build();

        System.out.println("[OBU Service] Logging out user: " + this.username);
        Auth.UserLogoutResponse logoutResponse = this.blockingStub.logoutUser(logoutRequest);
        if (logoutResponse.hasErrorMessage()) {
            throw new OBUException(logoutResponse.getErrorMessage().getDescription());
        }

        this.userToken = null;
        this.username = null;
        this.shiftId = null;
        this.routeId = 0;
        this.vehicleId = 0;
        this.resetData();
    }

    public void startShift(int routeId, int vehicleId) throws Exception {
        if (this.userToken == null) {
            throw new Exception("[OBU Service] Failed to login user, shutting down.");
        }

        Data.StartShiftRequest startShiftRequest = Data.StartShiftRequest.newBuilder()
                .setToken(this.userToken)
                .setUsername(this.username)
                .setRouteId(routeId)
                .setVehicleId(vehicleId)
                .build();

        Data.StartShiftResponse startShiftResponse = this.blockingStub.startShift(startShiftRequest);
        if (startShiftResponse.hasErrorMessage()) {
            throw new OBUException(startShiftResponse.getErrorMessage().getDescription());
        }

        this.shiftId = startShiftResponse.getShiftId();
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        System.out.println("[OBU Service] Started shift for user: " + this.username);
    }

    public void endShift() throws OBUException {
        Data.EndShiftRequest endShiftRequest = Data.EndShiftRequest.newBuilder()
                .setToken(this.userToken)
                .setUsername(this.username)
                .build();

        System.out.println("[OBU Service] Sending end shift request for user: " + this.username);
        Data.EndShiftResponse endShiftResponse = this.blockingStub.endShift(endShiftRequest);
        if (endShiftResponse.hasErrorMessage()) {
            throw new OBUException(endShiftResponse.getErrorMessage().getDescription());
        }

        this.shiftId = null;
        this.routeId = 0;
        this.vehicleId = 0;
        this.resetData();

        System.out.println("[OBU Service] Ended shift for user: " + this.username);
    }

    public void bulkEndShift(int iterations) {
        try {
            System.out.println("Waiting 2 minutes...");
            Thread.sleep(3 * 60 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Data.EndShiftRequest endShiftRequest = Data.EndShiftRequest.newBuilder()
                .setToken(this.userToken)
                .setUsername(this.username)
                .build();

        System.out.println("[OBU Service] Sending end shift request for user: " + this.username);
        long startTime = Instant.now().getEpochSecond();

        final int[] counter = {0};
        for (int i = 0; i < iterations; i++) {

            int finalI = i;
            this.asyncStub.endShift(endShiftRequest, new StreamObserver<Data.EndShiftResponse>() {
                @Override
                public void onNext(Data.EndShiftResponse endShiftResponse) {
                    if (endShiftResponse.hasErrorMessage()) {
                        System.err.println("[OBU Service] " + endShiftResponse.getErrorMessage());
                    }
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    if (counter[0] == iterations - 1) {
                        long endTime = Instant.now().getEpochSecond();
                        float throughput = (float) iterations / (endTime - startTime);
                        System.out.println("[OBU Service] Time: " +
                                TimeUnit.SECONDS.toSeconds(endTime - startTime));
                        System.out.println("[OBU Service] Service throughput: " + throughput);
                    } else {
                        counter[0] = counter[0] + 1;
                    }

                    System.out.println("[OBU Service] Response from API Gateways for iteration " + finalI);
                }
            });

            System.out.println("[OBU Service] Finished shift for user: " + this.username + ". Iteration #" + i);
        }
    }

    public void submitUserData() throws OBUException {
        Data.SubmitDataLogRequest submitDataLogRequest = getSubmitDataLogRequest();

        Data.SubmitDataLogResponse submitDataLogResponse = this.blockingStub.submitUserData(submitDataLogRequest);
        if (submitDataLogResponse.hasErrorMessage()) {
            throw new OBUException(submitDataLogResponse.getErrorMessage().getDescription());
        }

        this.resetData();
        System.out.println("[OBU Service] Submitted data for user: " + this.username);
    }

    public void bulkSubmitUserData(int iterations) throws OBUException {

        long startTime = Instant.now().getEpochSecond();

        for (int i = 0; i < iterations; i++) {
            Data.SubmitDataLogRequest submitDataLogRequest = getSubmitDataLogRequest();

            Data.SubmitDataLogResponse submitDataLogResponse = this.blockingStub.submitUserData(submitDataLogRequest);
            if (submitDataLogResponse.hasErrorMessage()) {
                throw new OBUException(submitDataLogResponse.getErrorMessage().getDescription());
            }

            System.out.println("[OBU Service] Submitted data for user: " + this.username + ". Iteration #" + i);
        }

        long endTime = Instant.now().getEpochSecond();
        float throughput = (float) iterations / (endTime - startTime);
        System.out.println("[OBU Service] Time: " + TimeUnit.SECONDS.toSeconds(endTime - startTime));
        System.out.println("[OBU Service] Service throughput: " + throughput);
        System.out.println("[OBU Service] Submitted bulk data for user: " + this.username);
    }

    public void asyncBulkSubmitUserData(int iterations) throws OBUException {
        long startTime = Instant.now().getEpochSecond();

        final int[] counter = {0};
        for (int i = 0; i < iterations; i++) {
            Data.SubmitDataLogRequest submitDataLogRequest = getSubmitDataLogRequest();

            int finalI = i;
            this.asyncStub.submitUserData(submitDataLogRequest, new StreamObserver<Data.SubmitDataLogResponse>() {
                @Override
                public void onNext(Data.SubmitDataLogResponse submitDataLogResponse) {
                    if (submitDataLogResponse.hasErrorMessage()) {
                        System.err.println("[OBU Service] " + submitDataLogResponse.getErrorMessage());
                    }
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    if (counter[0] == iterations - 1) {
                        long endTime = Instant.now().getEpochSecond();
                        float throughput = (float) iterations / (endTime - startTime);
                        System.out.println("[OBU Service] Time: " +
                                TimeUnit.SECONDS.toSeconds(endTime - startTime));
                        System.out.println("[OBU Service] Service throughput: " + throughput);
                    } else {
                        counter[0] = counter[0] + 1;
                    }

                    System.out.println("[OBU Service] Response from API Gateways for iteration " + finalI);
                }
            });

            System.out.println("[OBU Service] Submitted data for user: " + this.username + ". Iteration #" + i);
        }

        System.out.println("[OBU Service] Submitted bulk data for user: " + this.username);
    }

    private Data.SubmitDataLogRequest getSubmitDataLogRequest() {
        Data.DataRequest dataRequest = Data.DataRequest.newBuilder()
                .setDriverId(this.username)
                .setRouteId(this.routeId)
                .setVehicleId(this.vehicleId)
                .addAllBpm(this.bpm)
                .addAllDrowsiness(this.drowsiness)
                .addAllSpeeds(this.speeds)
                .addAllTimestamps(this.timestamps)
                .setShiftId(this.shiftId)
                .build();

        return Data.SubmitDataLogRequest.newBuilder()
                .setDataLogs(dataRequest)
                .setToken(this.userToken)
                .setShiftId(this.shiftId)
                .build();
    }

    public void addBpm(int bpm) {
        this.bpm.add(bpm);
    }

    public void addDrowsiness(int drowsiness) {
        this.drowsiness.add(drowsiness);
    }

    public void addSpeed(int speed) {
        this.speeds.add(speed);
    }

    public void addTimestamp(long timestamp) {
        this.timestamps.add(timestamp);
    }

    public void resetData() {
        this.bpm.clear();
        this.drowsiness.clear();
        this.speeds.clear();
        this.timestamps.clear();
    }

    @Override
    public void close() {
        this.channel.shutdown();
    }
}
