package models;

public class RabbitMqConfig {
    private final String rabbitMqAddress;
    private final String rabbitMqPort;
    private final String configQueue;

    public RabbitMqConfig(String rabbitMqAddress, String rabbitMqPort, String configQueue) {
        this.rabbitMqAddress = rabbitMqAddress;
        this.rabbitMqPort = rabbitMqPort;
        this.configQueue = configQueue;
    }

    public String getRabbitMqAddress() {
        return rabbitMqAddress;
    }

    public String getRabbitMqPort() {
        return rabbitMqPort;
    }

    public String getConfigQueue() {
        return configQueue;
    }
}
