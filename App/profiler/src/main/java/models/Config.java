package models;

import com.owlike.genson.annotation.JsonProperty;

public class Config {
    private final String channelName;
    private final String hyperledgerUserId;

    public Config(@JsonProperty("channelName") String channelName,
                  @JsonProperty("hyperledgerUserId") String hyperledgerUserId) {
        this.channelName = channelName;
        this.hyperledgerUserId = hyperledgerUserId;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getHyperledgerUserId() {
        return hyperledgerUserId;
    }
}
