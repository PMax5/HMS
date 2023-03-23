package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.Map;

public class Config {

    private final Map<String, String> serviceChannels;
    private final int serverPort;

    public Config(@JsonProperty("serviceChannels") Map<String, String> serviceChannels,
                  @JsonProperty("serverPort") int serverPort) {
        this.serviceChannels = serviceChannels;
        this.serverPort = serverPort;
    }

    public String getServiceChannel(String serviceId) {
        return this.serviceChannels.get(serviceId);
    }

    public int getServerPort() { return this.serverPort; }
}
