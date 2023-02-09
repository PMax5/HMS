package models;

import java.util.List;

public class DataLog {

    private final String userId;
    private final int routeId;
    private final int vehicleId;
    private final List<Integer> bpmValues;
    private final List<Integer> drowsinessValues;
    private final List<Integer> speedValues;
    private final List<Long> timestampValues;

    public DataLog(String userId, int routeId, int vehicleId, List<Integer> bpm, List<Integer> drowsiness,
                   List<Integer> averageSpeed, List<Long> timestamps) {
        this.userId = userId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.bpmValues = bpm;
        this.drowsinessValues = drowsiness;
        this.speedValues = averageSpeed;
        this.timestampValues = timestamps;
    }

    public String getUserId() {
        return this.userId;
    }

    public int getRouteId() {
        return this.routeId;
    }

    public int getVehicleId() {
        return this.vehicleId;
    }

    public List<Integer> getBpmValues() {
        return this.bpmValues;
    }

    public List<Integer> getDrowsinessValues() {
        return this.drowsinessValues;
    }

    public List<Integer> getSpeedValues() {
        return this.speedValues;
    }

    public List<Long> getTimestampValues() {
        return this.timestampValues;
    }
}
