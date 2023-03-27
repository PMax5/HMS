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
import java.util.stream.Collectors;

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
                    Data.SubmitDataLogRequest request = Data.SubmitDataLogRequest.parseFrom(delivery.getBody());

                    UserRole role = dataService.authorizeUser(request.getToken());
                    Data.DataResponse.Builder responseBuilder = Data.DataResponse.newBuilder();
                    if (role.equals(UserRole.DRIVER)) {
                        Data.DataRequest dataRequest = request.getDataLogs();

                        DataLog dataLog = dataService.submitUserData(
                                dataRequest.getDriverId(),
                                dataRequest.getRouteId(),
                                dataRequest.getVehicleId(),
                                dataRequest.getBpmList(),
                                dataRequest.getDrowsinessList(),
                                dataRequest.getSpeedsList(),
                                dataRequest.getTimestampsList()
                        );

                        if (dataLog == null) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                    .setDescription("[Data Service] Failed to submit log for user: " +
                                            dataRequest.getDriverId())
                                    .build());
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] User " + request.getDataLogs().getDriverId()
                                        + " is not allowed to submit logs.")
                                .build()
                        );
                    }

                    dataServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            dataServer.addOperationHandler(Operations.GET_USER_DATALOGS, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Data.GetDataLogRequest request = Data.GetDataLogRequest.parseFrom(delivery.getBody());

                    UserRole role = dataService.authorizeUser(request.getToken());
                    Data.GetDataLogResponse.Builder responseBuilder = Data.GetDataLogResponse.newBuilder();

                    if (role.equals(UserRole.HEALTH_STAFF)) {
                        List<DataLog> dataLogs = dataService.getDataLogsForUser(request.getUsername());

                        if (dataLogs == null) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                    .setDescription("[Data Service] Failed to get logs for user: " + request.getUsername())
                                    .build());
                        } else {
                            responseBuilder.addAllDataLogs(dataLogs
                                    .stream()
                                    .map(l -> Data.DataRequest.newBuilder()
                                            .setDriverId(l.getUserId())
                                            .setRouteId(l.getRouteId())
                                            .setVehicleId(l.getVehicleId())
                                            .addAllBpm(l.getBpmValues())
                                            .addAllDrowsiness(l.getDrowsinessValues())
                                            .addAllSpeeds(l.getSpeedValues())
                                            .addAllTimestamps(l.getTimestampValues())
                                            .setShiftId(l.getShiftId())
                                            .build())
                                    .collect(Collectors.toList())
                            );
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] User " + request.getUsername() + " is not allowed to " +
                                        "access this type of records.")
                                .build()
                        );
                    }

                    dataServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            dataServer.addOperationHandler(Operations.START_SHIFT_REQUEST, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Data.StartShiftRequest request = Data.StartShiftRequest.parseFrom(delivery.getBody());

                    UserRole role = dataService.authorizeUser(request.getToken());
                    Data.StartShiftResponse.Builder responseBuilder = Data.StartShiftResponse.newBuilder();

                    if (role.equals(UserRole.DRIVER)) {
                        String shiftId = dataService.startShift(request.getUsername());

                        if (shiftId != null) {
                            responseBuilder.setShiftId(shiftId);
                        } else {
                            responseBuilder.setErrorMessage(
                                    Auth.ErrorMessage.newBuilder()
                                            .setDescription("[Data Service] User " + request.getUsername() + " " +
                                                    "is not allowed to start a shift that already started.")
                                            .build()
                            );
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] User " + request.getUsername() + " is not allowed to start"
                                        + " a shift.")
                                .build()
                        );
                    }

                    dataServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                }
            });

            dataServer.addOperationHandler(Operations.END_SHIFT_REQUEST, new Operation() {
                @Override
                public void execute(String consumerTag, Delivery delivery) throws IOException {
                    Data.EndShiftRequest request = Data.EndShiftRequest.parseFrom(delivery.getBody());

                    UserRole role = dataService.authorizeUser(request.getToken());
                    Data.EndShiftResponse.Builder responseBuilder = Data.EndShiftResponse.newBuilder();

                    if (role.equals(UserRole.DRIVER)) {
                        boolean success = dataService.endShift(request.getUsername());
                        if (!success) {
                            responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                    .setDescription("[Data Service] User " + request.getUsername() + " is not allowed to end"
                                            + " a shift that does not exist.")
                                    .build()
                            );
                        }
                    } else {
                        responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                .setDescription("[Data Service] User " + request.getUsername() + " is not allowed to end" +
                                        "a shift.")
                                .build()
                        );
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
