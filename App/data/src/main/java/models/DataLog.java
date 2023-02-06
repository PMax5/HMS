package models;

import java.util.List;

public class DataLog {

    private final String userId;
    private final String routeId;
    private final String vehicleId;
    private final List<Integer> bpm;
    private final List<Integer> drowsiness;
    private final List<Integer> averageSpeed;
    private final List<Long> timestamps;

    public DataLog(String userId, String routeId, String vehicleId, List<Integer> bpm, List<Integer> drowsiness,
                   List<Integer> averageSpeed, List<Long> timestamps) {
        this.userId = userId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.bpm = bpm;
        this.drowsiness = drowsiness;
        this.averageSpeed = averageSpeed;
        this.timestamps = timestamps;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getRouteId() {
        return this.routeId;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public List<Integer> getBpm() {
        return this.bpm;
    }

    public List<Integer> getDrowsiness() {
        return this.drowsiness;
    }

    public List<Integer> getAverageSpeed() {
        return this.averageSpeed;
    }

    public List<Long> getTimestamps() {
        return this.timestamps;
    }
}
