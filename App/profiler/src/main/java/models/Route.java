package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.List;

public class Route {
    private int id;
    private List<Vehicle> vehicles;
    private List<RouteCharacteristic> characteristics;

    public Route(@JsonProperty("id") int id, @JsonProperty("vehicles") List<Vehicle> vehicles,
                 @JsonProperty("characteristics") List<RouteCharacteristic> characteristics) {
        this.id = id;
        this.vehicles = vehicles;
        this.characteristics = characteristics;
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
}
