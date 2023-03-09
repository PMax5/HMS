package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final String channelName;
    private final List<String> userIds;
    private final String userAffiliation;
    private final String mspId;

    public Config(@JsonProperty("channelName") String channelName,
                  @JsonProperty("userAffiliation") String userAffiliation, @JsonProperty("mspId") String mspId) {
        this.channelName = channelName;
        this.userIds = new ArrayList<>();
        this.userIds.add("dataUser");

        this.userAffiliation = userAffiliation;
        this.mspId = mspId;
    }

    public String getChannelName() {
        return this.channelName;
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
