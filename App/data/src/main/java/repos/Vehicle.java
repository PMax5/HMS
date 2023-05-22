package repos;

import com.owlike.genson.annotation.JsonProperty;

public class Vehicle {

    private int id;
    private VehicleType type;

    public Vehicle(@JsonProperty("id") int id, @JsonProperty("type") VehicleType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public VehicleType getType() {
        return this.type;
    }
}
