import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;
import java.util.Objects;

public class Profile {

    @Property()
    private final String id;

    @Property()
    private final List<Integer> ageRage;

    @Property()
    private final String gender;

    @Property()
    private final List<Integer> shiftHoursRage;

    @Property()
    private final List<String> shiftTypes;

    @Property()
    private final List<Integer> routeIds;

    public Profile(@JsonProperty("id") String id, @JsonProperty("ageRange") List<Integer> ageRage,
                   @JsonProperty("gender") String gender,
                   @JsonProperty("shiftHoursRage") List<Integer> shiftHoursRage,
                   @JsonProperty("shiftTypes") List<String> shiftTypes,
                   @JsonProperty("routeIds") List<Integer> routeIds) {
        this.id = id;
        this.ageRage = ageRage;
        this.gender = gender;
        this.shiftHoursRage = shiftHoursRage;
        this.shiftTypes = shiftTypes;
        this.routeIds = routeIds;
    }

    public String getId() {
        return this.id;
    }

    public Integer getMinAge() {
        return this.ageRage.get(0);
    }

    public Integer getMaxAge() {
        return this.ageRage.get(1);
    }

    public String getGender() {
        return this.gender;
    }

    public Integer getMinShiftHours() {
        return this.shiftHoursRage.get(0);
    }

    public Integer getMaxShiftHours() {
        return this.shiftHoursRage.get(1);
    }

    public List<String> getShiftTypes() {
        return this.shiftTypes;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }

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
                this.getShiftTypes()
        );
    }
}
