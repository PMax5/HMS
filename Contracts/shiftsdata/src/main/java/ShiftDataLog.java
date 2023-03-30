import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public class ShiftDataLog {

    @Property()
    private final String userId;

    @Property()
    private final String shiftId;

    @Property()
    private final int vehicleId;

    @Property()
    private final int routeId;

    @Property()
    private final int averageBPM;

    @Property()
    private final int averageDrowsiness;

    @Property()
    private final int averageSpeed;

    public ShiftDataLog(@JsonProperty("userId") String userId, @JsonProperty("shiftId") String shiftId,
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

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getUserId(),
                this.getShiftId(),
                this.getVehicleId(),
                this.getRouteId(),
                this.getAverageBPM(),
                this.getAverageDrowsiness(),
                this.getAverageSpeed()
        );
    }
}