import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import hmsProto.Auth;
import models.*;
import services.RabbitMqService;
import services.RegistryService;

import java.io.IOException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        try {
            RabbitMqService rabbitMqService = new RabbitMqService();
            RegistryService registryService = new RegistryService(rabbitMqService);

            Config config = registryService.loadServiceConfig();
            registryService.loadHyperledgerService();

            System.out.println("Initializing server...");
            RpcServer registryServer = rabbitMqService.newRpcServer(config.getChannelName());
            Channel channel = registryServer.getChannel();

            registryServer.addOperationHandler(Operations.NEW_USER_REQUEST, new Operation() {
                        @Override
                        public void execute(String consumerTag, Delivery delivery) throws IOException {
                            Auth.UserRegistrationRequest request = Auth.UserRegistrationRequest
                                    .parseFrom(delivery.getBody());

                            User user = registryService.registerUser(
                                    request.getUsername(),
                                    request.getName(),
                                    request.getAge(),
                                    Gender.valueOf(request.getGender().getValueDescriptor().getName()),
                                    UserRole.valueOf(request.getRole().getValueDescriptor().getName()),
                                    request.getPassword()
                            );

                            Auth.UserRegistrationResponse.Builder responseBuilder = Auth.UserRegistrationResponse.newBuilder();

                            if (user == null) {
                                responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                        .setDescription("[Registry Service] Failed to register user " + request.getUsername())
                                        .build());
                            } else {
                                responseBuilder.setUserdata(Auth.UserData.newBuilder()
                                        .setUsername(user.getUsername())
                                        .setName(user.getName())
                                        .setAge(user.getAge())
                                        .setGender(Auth.GENDER.valueOf(user.getGender().toString()))
                                        .setRole(Auth.ROLE.valueOf(user.getRole().toString()))
                                        .setProfileId(user.getProfileId())
                                        .addAllRouteIds(user.getRouteIds())
                                        .build());
                            }

                            registryServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                        }
                    }
            );

            DeliverCallback mainHandler = (consumerTag, delivery) -> {
                System.out.println("Received new operation request!");
                registryServer.executeOperationHandler(delivery);
            };

            channel.basicConsume(config.getChannelName(), false, mainHandler, (consumerTag -> {}));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}