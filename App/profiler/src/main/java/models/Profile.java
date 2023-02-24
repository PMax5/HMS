package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.List;

public class Profile {

    private int id;
    private List<Integer> ageRage;
    private Gender gender;
    private List<Integer> shiftHoursRage;
    private List<ShiftType> shiftTypes;
    private List<Integer> routeIds;

    public Profile(@JsonProperty("id") int id, @JsonProperty("ageRange") List<Integer> ageRage,
                   @JsonProperty("gender") String gender,
                   @JsonProperty("shiftHoursRage") List<Integer> shiftHoursRage,
                   @JsonProperty("shiftTypes") List<String> shiftTypes,
                   @JsonProperty("routeIds") List<Integer> routeIds) {
        this.id = id;
        this.ageRage = ageRage;
        this.gender = Gender.valueOf(gender);
        this.shiftHoursRage = shiftHoursRage;
        shiftTypes.forEach((shiftType) -> {
            this.shiftTypes.add(ShiftType.valueOf(shiftType));
        });
        this.routeIds = routeIds;
    }

    public int getId() {
        return this.id;
    }

    public Integer getMinAge() {
        return this.ageRage.get(0);
    }

    public Integer getMaxAge() {
        return this.ageRage.get(1);
    }

    public Gender getGender() {
        return this.gender;
    }

    public Integer getMinShiftHours() {
        return this.shiftHoursRage.get(0);
    }

    public Integer getMaxShiftHours() {
        return this.shiftHoursRage.get(1);
    }

    public List<ShiftType> getShiftTypes() {
        return this.shiftTypes;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }
}
