package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Profile {

    private String id;
    private List<Integer> ageRange;
    private Gender gender;
    private List<Integer> shiftHoursRange;
    private List<ShiftType> shiftTypes;
    private List<Integer> routeIds;
    private List<RouteCharacteristic> routeCharacteristics;

    public Profile(@JsonProperty("id") String id, @JsonProperty("ageRange") List<Integer> ageRange,
                   @JsonProperty("gender") String gender,
                   @JsonProperty("shiftHoursRage") List<Integer> shiftHoursRange,
                   @JsonProperty("shiftTypes") List<String> shiftTypes,
                   @JsonProperty("routeIds") List<Integer> routeIds,
                   @JsonProperty("routeCharacteristics") List<String> routeCharacteristics) {
        this.id = id;
        this.ageRange = ageRange;
        this.gender = Gender.valueOf(gender);
        this.shiftHoursRange = shiftHoursRange;
        this.shiftTypes = new ArrayList<>();
        shiftTypes.forEach((shiftType) -> {
            this.shiftTypes.add(ShiftType.valueOf(shiftType));
        });
        routeCharacteristics.forEach((routeCharacteristic) -> {
            this.routeCharacteristics.add(RouteCharacteristic.valueOf(routeCharacteristic));
        });

        this.routeIds = routeIds;
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

    public Gender getGender() {
        return this.gender;
    }

    public Integer getMinShiftHours() {
        return this.shiftHoursRange.get(0);
    }

    public Integer getMaxShiftHours() {
        return this.shiftHoursRange.get(1);
    }

    public List<ShiftType> getShiftTypes() {
        return this.shiftTypes;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }

    public List<RouteCharacteristic> getRouteCharacteristics() {
        return this.routeCharacteristics;
    }
}
