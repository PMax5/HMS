import services.HyperledgerService;
import services.RegistryService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        RegistryService registryService = new RegistryService();

        try {
            HyperledgerService hyperledgerService = new HyperledgerService();
            hyperledgerService.enrollAdminUser();

            registryService.loadServiceConfig();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}