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
    private int type;

    public Profile(@JsonProperty("id") String id, @JsonProperty("ageRange") List<Integer> ageRange,
                   @JsonProperty("gender") String gender,
                   @JsonProperty("shiftHoursRange") List<Integer> shiftHoursRange,
                   @JsonProperty("shiftTypes") List<String> shiftTypes,
                   @JsonProperty("routeIds") List<Integer> routeIds,
                   @JsonProperty("routeCharacteristics") List<String> routeCharacteristics,
                   @JsonProperty("type") int type) {
        this.id = id;
        this.ageRange = ageRange;
        this.gender = Gender.valueOf(gender);
        this.shiftHoursRange = shiftHoursRange;
        this.shiftTypes = new ArrayList<>();
        shiftTypes.forEach((shiftType) -> {
            this.shiftTypes.add(ShiftType.valueOf(shiftType));
        });

        this.routeCharacteristics = new ArrayList<>();
        routeCharacteristics.forEach((routeCharacteristic) -> {
            this.routeCharacteristics.add(RouteCharacteristic.valueOf(routeCharacteristic));
        });

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

    public int getType() { return this.type; }

    @Override
    public String toString() {
        return "=== PROFILE ===\n ID: " + this.getId() +
                "\n Shift Types: " + this.getShiftTypes() +
                "\n Route Characteristics: " + this.getRouteCharacteristics();
    }
}
