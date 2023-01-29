import services.RegistryService;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        try {
            RegistryService registryService = new RegistryService();
            //registryService.loadServiceConfig();
            registryService.loadHyperledgerService();
            registryService.registerUser();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}