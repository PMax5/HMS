package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private int id;
    private List<Vehicle> vehicles;
    private List<RouteCharacteristic> characteristics;
    private List<ShiftType> shiftTypes;
    private List<Integer> slots;

    public Route(@JsonProperty("id") int id, @JsonProperty("vehicles") List<Vehicle> vehicles,
                 @JsonProperty("characteristics") List<RouteCharacteristic> characteristics,
                 @JsonProperty("slots") List<Integer> slots,
                 @JsonProperty("shiftTypes") List<String> shiftTypes) {
        this.id = id;
        this.vehicles = vehicles;
        this.characteristics = characteristics;
        this.slots = slots;
        this.shiftTypes = new ArrayList<>();
        shiftTypes.forEach((shiftType) -> {
            this.shiftTypes.add(ShiftType.valueOf(shiftType));
        });
    }

    public int getId() {
        return this.id;
    }

    public List<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public List<RouteCharacteristic> getCharacteristics() {
        return this.characteristics;
    }

    public List<ShiftType> getShiftTypes() {
        return this.shiftTypes;
    }

    public List<Integer> getSlots() {
        return this.slots;
    }


}
