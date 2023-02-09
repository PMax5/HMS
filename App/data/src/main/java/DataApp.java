import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import models.Config;
import models.RpcServer;
import services.DataService;
import services.RabbitMqService;

public class DataApp {
    public static void main(String[] args) {
        try {
            RabbitMqService rabbitMqService = new RabbitMqService();
            DataService dataService = new DataService(rabbitMqService);

            Config config = dataService.loadServiceConfig();
            System.out.println("[Data App] Initializing server...");

            RpcServer dataServer = rabbitMqService.newRpcServer(config.getChannelName());
            Channel channel = dataServer.getChannel();

            // TODO: Implement service operations handlers.

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                System.out.println("[Data App] Received new operation request!");
                dataServer.executeOperationHandler(delivery);
            };

            channel.basicConsume(config.getChannelName(), false, mainHandler, (consumerTag -> {}));
        } catch (Exception e) {
            System.err.println("[Data App] Unexpected error occurred: " + e.getMessage());
        }
    }
}
