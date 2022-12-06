package models;

public class Config {
    private final String databaseAddress;
    private final String databasePort;
    private final String serviceId;

    public Config(String databaseAddress, String databasePort, String serviceId) {
        this.databaseAddress = databaseAddress;
        this.databasePort = databasePort;
        this.serviceId = serviceId;
    }

    public String getDatabaseAddress() {
        return databaseAddress;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    public String getServiceId() { return serviceId; }
}
