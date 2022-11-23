package services;

import models.Config;

public class ConfigService {

    public Config loadConfig() {
        return new Config(
                System.getenv("REGISTRY_DATABASE_ADDRESS"),
                System.getenv("REGISTRY_DATABASE_PORT")
        );
    }
}
