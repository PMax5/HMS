import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import hmsProto.Auth;
import hmsProto.Data;
import models.*;
import services.DataService;
import services.RabbitMqService;

import java.io.IOException;
import java.util.List;

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
                    Data.DataRequest request = Data.DataRequest.parseFrom(delivery.getBody());

                    DataLog dataLog = dataService.submitUserData(
                            request.getDriverId(),
                            request.getRouteId(),
                            request.getVehicleId(),
                            request.getBpmList(),
                            request.getDrowsinessList(),
                            request.getSpeedsList(),
                            request.getTimestampsList()
                    );

                    Data.DataResponse.Builder responseBuilder = Data.DataResponse.newBuilder();

                    if (dataLog == null) {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] Failed to submit log for user: " + request.getDriverId())
                                .build());
                    }

                    dataServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            dataServer.addOperationHandler(Operations.GET_USER_DATALOGS, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Data.GetDataLogRequest request = Data.GetDataLogRequest.parseFrom(delivery.getBody());

                    List<DataLog> dataLogs = dataService.getDataLogsForUser(request.getUsername());

                    Data.GetDataLogResponse.Builder responseBuilder = Data.GetDataLogResponse.newBuilder();

                    if (dataLogs == null) {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] Failed to get logs for user: " + request.getUsername())
                                .build());
                    }

                    dataServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
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
