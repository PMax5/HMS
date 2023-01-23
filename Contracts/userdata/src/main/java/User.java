import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

enum GENDER {
    MALE,
    FEMALE
}

@DataType()
public class User {

    @Property()
    private final String name;

    @Property()
    private final String username;

    @Property()
    private final GENDER gender;

    @Property()
    private final long createdAt;

    @Property()
    private int age;

    @Property()
    private int profileId;

    @Property()
    private List<Integer> routeIds;

    public User(@JsonProperty("name") final String name, @JsonProperty("username") final String username,
                @JsonProperty("age") final int age, @JsonProperty("gender") final GENDER gender) {
        this.name = name;
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.createdAt = Instant.now().getEpochSecond();
        this.routeIds = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() {
        return this.username;
    }

    public int getAge() {
        return this.age;
    }

    public GENDER getGender() {
        return this.gender;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public List<Integer> getRouteIds() {
        return this.routeIds;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void addRouteId(int routeId) {
        this.routeIds.add(routeId);
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
}
