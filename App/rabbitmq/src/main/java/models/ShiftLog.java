package models;

import com.owlike.genson.annotation.JsonProperty;

public class ShiftLog {

    private final String userId;
    private final String shiftId;
    private final int vehicleId;
    private final int routeId;
    private final int averageBPM;
    private final int averageDrowsiness;
    private final int averageSpeed;

    public ShiftLog(@JsonProperty("userId") String userId, @JsonProperty("shiftId") String shiftId,
                    @JsonProperty("vehicleId") int vehicleId, @JsonProperty("routeId") int routeId,
                    @JsonProperty("averageBPM") int averageBPM,
                    @JsonProperty("averageDrowsiness") int averageDrowsiness,
                    @JsonProperty("averageSpeed") int averageSpeed) {
        this.userId = userId;
        this.shiftId = shiftId;
        this.vehicleId = vehicleId;
        this.routeId = routeId;
        this.averageBPM = averageBPM;
        this.averageDrowsiness = averageDrowsiness;
        this.averageSpeed = averageSpeed;
    }

    public String getUserId() { return this.userId; }

    public String getShiftId() { return this.shiftId; }

    public int getVehicleId() { return this.vehicleId; }

    public int getRouteId() { return this.routeId; }

    public int getAverageBPM() { return this.averageBPM; }

    public int getAverageDrowsiness() { return this.averageDrowsiness; }

    public int getAverageSpeed() { return this.averageSpeed; }
}
