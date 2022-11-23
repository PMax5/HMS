package models;

public class RabbitMqConfig {
    private final String rabbitMqAddress;
    private final String rabbitMqPort;
    private final String rabbitMqUserName;
    private final String rabbitMqPassword;
    private final String virtualHost;
    private final String configQueue;

    public RabbitMqConfig(String rabbitMqAddress, String rabbitMqPort, String rabbitMqUserName, String rabbitMqPassword, String virtualHost, String configQueue) {
        this.rabbitMqAddress = rabbitMqAddress;
        this.rabbitMqPort = rabbitMqPort;
        this.rabbitMqUserName = rabbitMqUserName;
        this.rabbitMqPassword = rabbitMqPassword;
        this.virtualHost = virtualHost;
        this.configQueue = configQueue;
    }

    public String getRabbitMqAddress() {
        return rabbitMqAddress;
    }

    public String getRabbitMqPort() {
        return rabbitMqPort;
    }

    public String getRabbitMqUserName() {
        return rabbitMqUserName;
    }

    public String getRabbitMqPassword() {
        return rabbitMqPassword;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getConfigQueue() {
        return configQueue;
    }
}
