package services;

import exceptions.OBUException;
import hmsProto.Auth;
import hmsProto.Data;
import hmsProto.GatewaysGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;

public class OBUService implements AutoCloseable {

    private final GatewaysGrpc.GatewaysBlockingStub stub;
    private final ManagedChannel channel;
    private String userToken;
    private String username;
    private String shiftId;
    private int routeId;
    private int vehicleId;
    private List<Integer> bpm;
    private List<Integer> drowsiness;
    private List<Integer> speeds;
    private List<Long> timestamps;

    public OBUService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = GatewaysGrpc.newBlockingStub(this.channel);
    }

    public void loginUser(String username, String password) throws OBUException {
        Auth.UserAuthenticationRequest authenticationRequest = Auth.UserAuthenticationRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        Auth.UserAuthenticationResponse authenticationResponse = this.stub.authenticateUser(authenticationRequest);
        if (authenticationResponse.hasErrorMessage()) {
            throw new OBUException(authenticationResponse.getErrorMessage().getDescription());
        }

        this.userToken = authenticationResponse.getToken();
        this.username = username;
    }

    public void logoutUser() throws OBUException {
        Auth.UserLogoutRequest logoutRequest = Auth.UserLogoutRequest.newBuilder()
                .setToken(this.userToken)
                .build();

        Auth.UserLogoutResponse logoutResponse = this.stub.logoutUser(logoutRequest);
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

    public void startShift(int routeId, int vehicleId) throws OBUException {
        Data.StartShiftRequest startShiftRequest = Data.StartShiftRequest.newBuilder()
                .setToken(this.userToken)
                .setUsername(this.username)
                .setRouteId(routeId)
                .setVehicleId(vehicleId)
                .build();

        Data.StartShiftResponse startShiftResponse = this.stub.startShift(startShiftRequest);
        if (startShiftResponse.hasErrorMessage()) {
            throw new OBUException(startShiftResponse.getErrorMessage().getDescription());
        }

        this.shiftId = startShiftResponse.getShiftId();
    }

    public void endShift() throws OBUException {
        Data.EndShiftRequest endShiftRequest = Data.EndShiftRequest.newBuilder()
                .setToken(this.userToken)
                .setUsername(this.username)
                .build();

        Data.EndShiftResponse endShiftResponse = this.stub.endShift(endShiftRequest);
        if (endShiftResponse.hasErrorMessage()) {
            throw new OBUException(endShiftResponse.getErrorMessage().getDescription());
        }

        this.shiftId = null;
        this.routeId = 0;
        this.vehicleId = 0;
        this.resetData();
    }

    public void submitUserData() throws OBUException {
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

        Data.SubmitDataLogRequest submitDataLogRequest = Data.SubmitDataLogRequest.newBuilder()
                .setDataLogs(dataRequest)
                .setToken(this.userToken)
                .setShiftId(this.shiftId)
                .build();

        Data.SubmitDataLogResponse submitDataLogResponse = this.stub.submitUserData(submitDataLogRequest);
        if (submitDataLogResponse.hasErrorMessage()) {
            throw new OBUException(submitDataLogResponse.getErrorMessage().getDescription());
        }

        this.resetData();
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
