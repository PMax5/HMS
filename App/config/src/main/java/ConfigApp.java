import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import hmsProto.Config;
import models.Operation;
import models.Operations;
import models.RpcServer;
import services.ConfigService;
import services.RabbitMqService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConfigApp {
    public static void main(String[] args) {

        RabbitMqService rabbitMqService = new RabbitMqService();
        ConfigService configService = new ConfigService();

        try {
            final String configQueueName = rabbitMqService.getRabbitMqConfig().getConfigQueue();
            RpcServer configServer = rabbitMqService.newRpcServer(configQueueName);
            Channel channel = configServer.getChannel();

            System.out.println("[Config App] Initializing server...");
            configServer.addOperationHandler(Operations.CONFIG_REQUEST, new Operation() {
                    @Override
                    public void execute(String consumerTag, Delivery delivery) throws IOException {
                        Config.GetConfigRequest request = Config.GetConfigRequest.parseFrom(delivery.getBody());
                        Config.GetConfigResponse response = Config.GetConfigResponse.newBuilder()
                                .setServiceConfig(configService.getConfig(request.getServiceId()))
                                .build();

                        System.out.println("[Config App] Received new operation request from service: " +
                                request.getServiceId());
                        configServer.sendResponseAndAck(delivery, response.toByteArray());
                    }
                }
            );

            configServer.addOperationHandler(Operations.SET_CONFIG_REQUEST, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Config.SetConfigRequest request = Config.SetConfigRequest.parseFrom(delivery.getBody());
                    Config.SetConfigResponse response = Config.SetConfigResponse.newBuilder().build();

                    configService.setConfig(request.getServiceId(), request.getServiceConfig());
                    configServer.sendResponseAndAck(delivery, response.toByteArray());
                }
            });

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                System.out.println("[Config App] Received a new request!");
                configServer.executeOperationHandler(delivery);
            };

            channel.basicConsume(configQueueName, false, mainHandler, (consumerTag -> {}));
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            System.err.println("[Config App] Unexpected error occurred: " + e.getMessage());
        }
    }
}
