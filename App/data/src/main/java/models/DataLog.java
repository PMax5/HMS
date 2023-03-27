package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.List;

public class DataLog {

    private final String userId;
    private final int routeId;
    private final int vehicleId;
    private final List<Integer> bpmValues;
    private final List<Integer> drowsinessValues;
    private final List<Integer> speedValues;
    private final List<Long> timestampValues;
    private final String shiftId;

    public DataLog(@JsonProperty("userId") String userId, @JsonProperty("routeId") int routeId,
                   @JsonProperty("vehicleId") int vehicleId, @JsonProperty("bpmValues") List<Integer> bpm,
                   @JsonProperty("drowsinessValues") List<Integer> drowsiness,
                   @JsonProperty("speedValues") List<Integer> averageSpeed,
                   @JsonProperty("timestampValues") List<Long> timestamps,
                   @JsonProperty("shiftId") String shiftId) {
        this.userId = userId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.bpmValues = bpm;
        this.drowsinessValues = drowsiness;
        this.speedValues = averageSpeed;
        this.timestampValues = timestamps;
        this.shiftId = shiftId;
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

    public String getShiftId() { return this.shiftId; }
}
