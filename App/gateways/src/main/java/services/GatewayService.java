package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import hmsProto.Auth;
import hmsProto.Data;
import hmsProto.Profiler;
import io.grpc.stub.StreamObserver;
import models.Config;
import models.Operations;
import models.RpcClient;

import hmsProto.GatewaysGrpc;
import hmsProto.GatewaysOuterClass.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GatewayService extends GatewaysGrpc.GatewaysImplBase {

    private final RabbitMqService rabbitMqService;
    private final static String SERVICE_ID = "service_gateways";
    private Config config;

    public GatewayService(RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
    }

    private RpcClient getRpcClient(String queueName) throws IOException, TimeoutException {
        Channel channel = rabbitMqService.createNewChannel();
        return new RpcClient(new RpcClientParams().channel(channel), queueName);
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        RpcClient rpcClient = this.getRpcClient(configQueueName);

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(SERVICE_ID)
                .build();

        final byte[] response = rpcClient.sendRequest(
                configQueueName,
                rpcClient.getChannel(),
                Operations.CONFIG_REQUEST,
                configRequest.toByteArray()
        );

        rpcClient.close();
        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);
        return this.config;
    }

    @Override
    public void registerUser(Auth.UserRegistrationRequest request,
                             StreamObserver<Auth.UserRegistrationResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_registry");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.NEW_USER_REQUEST,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Auth.UserRegistrationResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while registering a user: " + e.getMessage());
        }
    }

    @Override
    public void authenticateUser(Auth.UserAuthenticationRequest request,
                                 StreamObserver<Auth.UserAuthenticationResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_registry");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.AUTHENTICATION_REQUEST,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Auth.UserAuthenticationResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while authenticating a user: " + e.getMessage());
        }
    }

    @Override
    public void logoutUser(Auth.UserLogoutRequest request,
                                 StreamObserver<Auth.UserLogoutResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_registry");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.LOGOUT_REQUEST,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Auth.UserLogoutResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while logging out a user: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(Auth.UserDeleteRequest request,
                                 StreamObserver<Auth.UserDeleteResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_registry");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.DELETE_USER_REQUEST,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Auth.UserDeleteResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while deleting a user: " + e.getMessage());
        }
    }

    @Override
    public void getUserData(Data.GetDataLogRequest request,
                           StreamObserver<Data.GetDataLogResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_data");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.GET_USER_DATALOGS,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Data.GetDataLogResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while fetching data logs from a user: "
                    + e.getMessage());
        }
    }

    @Override
    public void submitUserData(Data.SubmitDataLogRequest request,
                            StreamObserver<Data.SubmitDataLogResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_data");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.SUBMIT_USER_DATALOG,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Data.SubmitDataLogResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while submitting data logs for a user: "
                    + e.getMessage());
        }
    }

    @Override
    public void registerProfile(Profiler.RegisterProfileRequest request,
                                StreamObserver<Profiler.RegisterProfileResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_profiler");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.REGISTER_PROFILE,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Profiler.RegisterProfileResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while registering a profile: "
                    + e.getMessage());
        }
    }

    @Override
    public void getProfiles(Profiler.GetProfilesRequest request,
                            StreamObserver<Profiler.GetProfilesResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_profiler");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.GET_PROFILES,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Profiler.GetProfilesResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while fetching user profiles: "
                    + e.getMessage());
        }
    }

    @Override
    public void setUserProfile(Profiler.SetProfileRequest request,
                            StreamObserver<Profiler.SetProfileResponse> responseObserver) {
        String serviceQueueName = this.config.getServiceChannel("service_profiler");
        try {
            RpcClient rpcClient = this.getRpcClient(serviceQueueName);
            final byte[] response = rpcClient.sendRequest(
                    serviceQueueName,
                    rpcClient.getChannel(),
                    Operations.SET_USER_PROFILE,
                    request.toByteArray()
            );
            rpcClient.close();

            responseObserver.onNext(Profiler.SetProfileResponse.parseFrom(response));
            responseObserver.onCompleted();
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException e) {
            System.err.println("[Gateway Service] An error occurred while setting up a profile for a user: "
                    + e.getMessage());
        }
    }

}
