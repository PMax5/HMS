package services;

import models.Config;

public class ConfigService {

    private final String serviceId;

    public ConfigService() {
        this.serviceId = "service_registry";
    }

    public Config loadConfig() {
        return new Config(
                System.getenv("REGISTRY_DATABASE_ADDRESS"),
                System.getenv("REGISTRY_DATABASE_PORT")
        );
    }

    public String getServiceId() {
        return serviceId;
    }
}
