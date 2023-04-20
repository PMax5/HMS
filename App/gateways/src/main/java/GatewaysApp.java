import io.grpc.Server;
import io.grpc.ServerBuilder;
import models.Config;
import services.GatewayService;
import services.RabbitMqService;

public class GatewaysApp {

    public static void main(String[] args) {
        try {
            RabbitMqService rabbitMqService = new RabbitMqService();
            GatewayService gatewayService = new GatewayService(rabbitMqService);

            Config config = gatewayService.loadServiceConfig();
            Server server = ServerBuilder
                    .forPort(config.getServerPort())
                    .addService(gatewayService)
                    .build();

            server.start();
            System.out.println("[Gateways App] Starting server on port " + config.getServerPort());
            server.awaitTermination();
        } catch (Exception e) {
            System.err.println("[Gateways App] Unexpected error occurred: " + e.getMessage());
        }
    }
}
