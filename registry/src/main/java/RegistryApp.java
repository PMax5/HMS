import services.ConfigService;
import services.RegistryService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        RegistryService registryService = new RegistryService();

        try {
            registryService.loadServiceConfig();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}