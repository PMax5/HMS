import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class UserQueryResult {

    @Property()
    private final String key;

    @Property()
    private final User record;

    public UserQueryResult(@JsonProperty("Key") final String key, @JsonProperty("Record") final User record) {
        this.key = key;
        this.record = record;
    }

    public String getKey() {
        return key;
    }

    public User getRecord() {
        return record;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getKey(), this.getRecord());
    }
}
