package services;

import models.Config;
import models.RabbitMqConfig;

public class ConfigService {

    public RabbitMqConfig loadRabbitMqConfig() {
        return new RabbitMqConfig(
                System.getenv("REGISTRY_RABBITMQ_ADDRESS"),
                System.getenv("REGISTRY_RABBITMQ_PORT"),
                System.getenv("REGISTRY_RABBITMQ_USERNAME"),
                System.getenv("REGISTRY_RABBITMQ_PASSWORD"),
                System.getenv("REGISTRY_RABBITMQ_VIRTUALHOST"),
                System.getenv("REGISTRY_RABBITMQ_CONFIG_QUEUE")
        );
    }

    public Config loadConfig() {
        return new Config(
                System.getenv("REGISTRY_DATABASE_ADDRESS"),
                System.getenv("REGISTRY_DATABASE_PORT")
        );
    }
}
