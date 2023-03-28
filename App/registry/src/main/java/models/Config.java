package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.List;

public class Config {
    private final List<String> userIds;
    private final String userAffiliation;
    private final String mspId;

    public Config(@JsonProperty("userAffiliation") String userAffiliation, @JsonProperty("mspId") String mspId,
                  @JsonProperty("userIds") List<String> userIds) {
        this.userAffiliation = userAffiliation;
        this.mspId = mspId;
        this.userIds = userIds;
    }

    public List<String> getServiceUsers() {
        return this.userIds;
    }

    public String getUserAffiliation() {
        return this.userAffiliation;
    }

    public String getMspId() {
        return this.mspId;
    }
}
