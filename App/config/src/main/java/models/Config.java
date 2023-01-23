package models;

public class Config {
    private final String databaseAddress;
    private final int databasePort;

    public Config(String databaseAddress, int databasePort) {
        this.databaseAddress = databaseAddress;
        this.databasePort = databasePort;
    }

    public String getDatabaseAddress() {
        return databaseAddress;
    }

    public int getDatabasePort() {
        return databasePort;
    }
}
