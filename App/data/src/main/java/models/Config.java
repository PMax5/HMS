package models;

public class Config {
    private final String channelName;
    private final String hyperledgerUserId;

    public Config(String channelName, String hyperledgerUserId) {
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
