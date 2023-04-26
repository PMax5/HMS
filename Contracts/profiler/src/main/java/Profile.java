import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;
import java.util.Objects;

@DataType()
public class Profile {

    @Property()
    private final String id;

    @Property()
    private final List<Integer> ageRange;

    @Property()
    private final String gender;

    @Property()
    private final List<Integer> shiftHoursRange;

    @Property()
    private final List<String> shiftTypes;

    @Property()
    private final List<Integer> routeIds;

    @Property()
    private final int type;

    public Profile(@JsonProperty("id") String id, @JsonProperty("ageRange") List<Integer> ageRange,
                   @JsonProperty("gender") String gender,
                   @JsonProperty("shiftHoursRange") List<Integer> shiftHoursRange,
                   @JsonProperty("shiftTypes") List<String> shiftTypes,
                   @JsonProperty("routeIds") List<Integer> routeIds,
                   @JsonProperty("type") int type) {
        this.id = id;
        this.ageRange = ageRange;
        this.gender = gender;
        this.shiftHoursRange = shiftHoursRange;
        this.shiftTypes = shiftTypes;
        this.routeIds = routeIds;
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public Integer getMinAge() {
        return this.ageRange.get(0);
    }

    public Integer getMaxAge() {
        return this.ageRange.get(1);
    }

    public String getGender() {
        return this.gender;
    }

    public Integer getMinShiftHours() {
        return this.shiftHoursRange.get(0);
    }

    public Integer getMaxShiftHours() {
        return this.shiftHoursRange.get(1);
    }

    public List<String> getShiftTypes() {
        return this.shiftTypes;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }

    public int getType() { return this.type; }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getId(),
                this.getGender(),
                this.getRouteIds(),
                this.getMaxAge(),
                this.getMaxShiftHours(),
                this.getMinAge(),
                this.getMinShiftHours(),
                this.getShiftTypes(),
                this.getType()
        );
    }
}
