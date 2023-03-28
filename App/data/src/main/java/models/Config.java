package models;

import com.owlike.genson.annotation.JsonProperty;

public class Config {
    private final String hyperledgerUserId;

    public Config(@JsonProperty("hyperledgerUserId") String hyperledgerUserId) {
        this.hyperledgerUserId = hyperledgerUserId;
    }

    public String getHyperledgerUserId() {
        return hyperledgerUserId;
    }
}
