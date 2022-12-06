package models;

public class Config {
    private final String databaseAddress;
    private final String databasePort;

    public Config(String databaseAddress, String databasePort) {
        this.databaseAddress = databaseAddress;
        this.databasePort = databasePort;
    }

    public String getDatabaseAddress() {
        return databaseAddress;
    }

    public String getDatabasePort() {
        return databasePort;
    }
}
