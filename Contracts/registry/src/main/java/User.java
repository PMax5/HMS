import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@DataType()
public final class User {

    @Property()
    private final String name;

    @Property()
    private final String username;

    @Property()
    private final String gender;

    @Property()
    private final String role;

    @Property()
    private final long createdAt;

    @Property()
    private final int age;

    @Property()
    private String hashedPassword;

    @Property()
    private int profileId;

    @Property()
    private final List<Integer> routeIds;

    public User(@JsonProperty("name") final String name, @JsonProperty("username") final String username,
                @JsonProperty("age") final int age, @JsonProperty("gender") final String gender,
                @JsonProperty("role") final String role,
                @JsonProperty("hashedPassword") final String hashedPassword) {
        this.name = name;
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.createdAt = Instant.now().getEpochSecond();
        this.routeIds = new ArrayList<>();
        this.hashedPassword = hashedPassword;
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

    public String getGender() {
        return this.gender;
    }

    public String getRole() {
        return this.role;
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

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void addRouteId(int routeId) {
        this.routeIds.add(routeId);
    }

    public void removeRouteId(int routeId) {
        this.routeIds.remove(routeId);
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getName(),
                this.getUsername(),
                this.getAge(),
                this.getGender(),
                this.getRole(),
                this.getProfileId(),
                this.getRouteIds(),
                this.getCreatedAt()
        );
    }
}
