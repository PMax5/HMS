package repos;

public class ConfigRepo {
    private final String databaseAddress;
    private final int databasePort;

    public ConfigRepo() {
        this.databaseAddress = System.getenv("CONFIG_DATABASE_ADDRESS");
        this.databasePort = Integer.parseInt(System.getenv("CONFIG_DATABASE_ADDRESS"));
    }

    private void newConnection() {
        // TODO: Create new connection to MongoDB
    }
}
