package models;

public class Shift {

    private final String userId;
    private final String shiftId;
    private final int routeId;
    private final int vehicleId;

    public Shift(String userId, String shiftId, int routeId, int vehicleId) {
        this.userId = userId;
        this.shiftId = shiftId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
    }

    public String getUserId() { return this.userId; }

    public String getShiftId() { return this.shiftId; }

    public int getRouteId() { return this.routeId; }

    public int getVehicleId() { return this.vehicleId; }
}
