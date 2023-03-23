package models;

import com.owlike.genson.annotation.JsonProperty;

import java.util.Map;

public class Config {

    private final Map<String, String> serviceChannels;

    public Config(@JsonProperty("serviceChannels") Map<String, String> serviceChannels) {
        this.serviceChannels = serviceChannels;
    }

    public String getServiceChannel(String serviceId) {
        return this.serviceChannels.get(serviceId);
    }
}
