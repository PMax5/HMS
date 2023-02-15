package models;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final String channelName;
    private final List<String> userIds;
    private final String userAffiliation;
    private final String mspId;

    public Config(String channelName) {
        this.channelName = channelName;
        this.userIds = new ArrayList<>();
        this.userIds.add("dataUser");

        this.userAffiliation = "org1.department1";
        this.mspId = "Org1MSP";
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
