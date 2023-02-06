package models;

public class Config {
    private final String channelName;

    public Config(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return this.channelName;
    }
}
