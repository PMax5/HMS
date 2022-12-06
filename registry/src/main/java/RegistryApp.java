import services.ConfigService;
import services.RabbitMqService;
import services.RegistryService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        ConfigService configService = new ConfigService();
        RegistryService registryService = new RegistryService(configService.loadConfig());

        try {
            registryService.loadServiceConfig();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}