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

            registryServer.addOperationHandler(Operations.AUTHENTICATION_REQUEST, new Operation() {
                        @Override
                        public void execute(String consumerTag, Delivery delivery) throws IOException {
                            Auth.UserAuthenticationRequest request = Auth.UserAuthenticationRequest
                                    .parseFrom(delivery.getBody());

                            String token = registryService.authenticateUser(
                                    request.getUsername(),
                                    request.getPassword()
                            );

                            Auth.UserAuthenticationResponse.Builder responseBuilder = Auth.UserAuthenticationResponse.newBuilder();

                            if (token == null) {
                                responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                        .setDescription("[Registry Service] Failed to login user " + request.getUsername())
                                        .build());
                            } else {
                                User user = registryService.getUserByToken(token);
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

            registryServer.addOperationHandler(Operations.AUTHORIZATION_REQUEST, new Operation() {
                        @Override
                        public void execute(String consumerTag, Delivery delivery) throws IOException {
                            Auth.UserAuthorizationRequest request = Auth.UserAuthorizationRequest
                                    .parseFrom(delivery.getBody());

                            UserRole role = registryService.authorizeUser(
                                    request.getToken()
                            );

                            Auth.UserAuthorizationResponse.Builder responseBuilder = Auth.UserAuthorizationResponse.newBuilder();

                            if (role == null) {
                                responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                        .setDescription("[Registry Service] Failed to authorize user with token: " + request.getToken())
                                        .build());
                            } else {
                                responseBuilder.setRole(Auth.ROLE.valueOf(role.toString()));
                            }

                            registryServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                        }
                    }
            );

            registryServer.addOperationHandler(Operations.LOGOUT_REQUEST, new Operation() {
                        @Override
                        public void execute(String consumerTag, Delivery delivery) throws IOException {
                            Auth.UserLogoutRequest request = Auth.UserLogoutRequest
                                    .parseFrom(delivery.getBody());

                            boolean success = registryService.logoutUser(
                                    request.getToken()
                            );

                            Auth.UserLogoutResponse.Builder responseBuilder = Auth.UserLogoutResponse.newBuilder();

                            if (!success) {
                                responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                        .setDescription("[Registry Service] Failed to logout user with token: " + request.getToken())
                                        .build());
                            }

                            registryServer.sendResponseAndAck(delivery, responseBuilder.build().toByteArray());
                        }
                    }
            );

            registryServer.addOperationHandler(Operations.DELETE_USER_REQUEST, new Operation() {
                        @Override
                        public void execute(String consumerTag, Delivery delivery) throws IOException {
                            Auth.UserDeleteRequest request = Auth.UserDeleteRequest
                                    .parseFrom(delivery.getBody());

                            boolean success = registryService.deleteUser(
                                    request.getToken(),
                                    request.getUsername()
                            );

                            Auth.UserDeleteResponse.Builder responseBuilder = Auth.UserDeleteResponse.newBuilder();

                            if (!success) {
                                responseBuilder.setErrorMessage(Auth.ErrorMessage.newBuilder()
                                        .setDescription("[Registry Service] Failed to delete user with username: " +
                                                request.getUsername())
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
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }
}