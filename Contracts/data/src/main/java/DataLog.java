import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@DataType()
public final class DataLog {

    @Property()
    private final String driverId;

    @Property()
    private final String routeId;

    @Property()
    private final String vehicleId;

    @Property()
    private final List<Integer> bpmValues;

    @Property()
    private final List<Integer> drowsinessValues;

    @Property()
    private final List<Integer> speedValues;

    @Property()
    private final List<Long> timestampValues;

    public DataLog(@JsonProperty("driverId") final String driverId, @JsonProperty("routeId") final String routeId,
                   @JsonProperty("vehicleId") final String vehicleId, @JsonProperty("bpmValues") final List<Integer> bpmValues,
                   @JsonProperty("drowsinessValues") final List<Integer> drowsinessValues,
                   @JsonProperty("speedValues") final List<Integer> speedValues,
                   @JsonProperty("timestampValues") final List<Long> timestampValues) {
        this.driverId = driverId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.bpmValues = bpmValues;
        this.drowsinessValues = drowsinessValues;
        this.speedValues = speedValues;
        this.timestampValues = timestampValues;
    }

    public String getDriverId() {
        return this.driverId;
    }

    public String getRouteId() {
        return this.routeId;
    }

    public String getVehicleId() {
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

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getDriverId(),
                this.getRouteId(),
                this.getVehicleId(),
                this.getBpmValues(),
                this.getDrowsinessValues(),
                this.getSpeedValues(),
                this.getTimestampValues()
        );
    }
}