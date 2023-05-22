package models;

import com.owlike.genson.annotation.JsonProperty;

public class Config {
    private final String hyperledgerUserId;
    private String databaseAddress;

    public Config(@JsonProperty("hyperledgerUserId") String hyperledgerUserId) {
        this.hyperledgerUserId = hyperledgerUserId;
    }

    public String getHyperledgerUserId() {
        return hyperledgerUserId;
    }

    public void setDatabaseAddress(String address) {
        this.databaseAddress =  address;
    }

    public String getDatabaseAddress() { return this.databaseAddress; }
}
