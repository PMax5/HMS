import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import hmsProto.Auth;
import models.Config;
import models.Operation;
import models.Operations;
import models.RpcServer;
import services.DataService;
import services.RabbitMqService;

import java.io.IOException;

public class DataApp {
    public static void main(String[] args) {
        try {
            RabbitMqService rabbitMqService = new RabbitMqService();
            DataService dataService = new DataService(rabbitMqService);

            Config config = dataService.loadServiceConfig();
            System.out.println("[Data App] Initializing server...");

            RpcServer dataServer = rabbitMqService.newRpcServer(config.getChannelName());
            Channel channel = dataServer.getChannel();

            dataServer.addOperationHandler(Operations.SUBMIT_USER_DATALOG, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    // TODO: Implement this method.
                }
            });

            dataServer.addOperationHandler(Operations.GET_USER_DATALOGS, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    // TODO: Implement this method.
                }
            });

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
