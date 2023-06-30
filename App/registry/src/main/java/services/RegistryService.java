package services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClientParams;
import models.*;
import org.hyperledger.fabric.gateway.ContractException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryService {

    private final HyperledgerService hyperledgerService;
    private final static String SERVICE_ID = "service_registry";
    private final static int TOKEN_LENGTH = 32;
    private final Map<String, User> authenticatedUsers;
    private Config config;
    private RabbitMqService rabbitMqService;

    public RegistryService(RabbitMqService rabbitMqService) throws Exception {
        this.rabbitMqService = rabbitMqService;
        this.hyperledgerService = new HyperledgerService();
        this.authenticatedUsers = new HashMap<>();
    }

    public RegistryService(Config config) throws Exception {
        this.config = config;
        this.hyperledgerService = new HyperledgerService();
        this.authenticatedUsers = new HashMap<>();
    }

    public Config loadServiceConfig() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        String configQueueName = this.rabbitMqService.getRabbitMqConfig().getConfigQueue();
        Channel channel = this.rabbitMqService.createNewChannel();
        RpcClient rpcClient = new RpcClient(new RpcClientParams().channel(channel));

        hmsProto.Config.GetConfigRequest configRequest = hmsProto.Config.GetConfigRequest.newBuilder()
                .setServiceId(SERVICE_ID)
                .build();

        final byte[] response = rpcClient.sendRequest(
                configQueueName,
                channel,
                Operations.CONFIG_REQUEST,
                configRequest.toByteArray()
        );
        rpcClient.close();

        hmsProto.Config.GetConfigResponse configResponse = hmsProto.Config.GetConfigResponse.parseFrom(response);
        this.config = new Gson().fromJson(configResponse.getServiceConfig(), Config.class);

        return this.config;
    }

    public void loadHyperledgerService() throws Exception {
        System.out.println("[Registry Service] Enrolling users...");
        this.hyperledgerService.enrollAdminUser();
        this.hyperledgerService.registerServicesUsers(
                this.config.getServiceUsers(),
                this.config.getUserAffiliation(),
                this.config.getMspId()
        );
    }

    public User registerUser(String username, String name, int age,
                             Gender gender, UserRole userRole, String hashedPassword) {
        try {
            System.out.println("[Registry Service] Registering user:\n" +
                    "Username: " + username +
                    "\nName: " +  name +
                    "\nAge: " + age +
                    "\nGender: " + gender +
                    "\nRole: " + userRole +
                    "\nPassword: " + hashedPassword
            );

            if (username.isEmpty() || name.isEmpty() || age < 18 || age > 100 || gender == null || userRole == null
                    || hashedPassword.isEmpty()) {
                System.out.println("[Registry Service] Failed to register user with username: " + username + ". " +
                        "Invalid data provided.");
                return null;
            }

            User user = this.hyperledgerService.registerUser(
                    username,
                    name,
                    age,
                    gender,
                    userRole,
                    hashedPassword
            );

            if (user != null) {
                System.out.println("[Registry Service] Successfully registered user with username: " + username);
            } else {
                System.out.println("[Registry Service] Failed to register user with username: " + username + ". " +
                        "Invalid data provided.");
            }

            return user;
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Registry Service] Failed to register user: " + e.getMessage());
            return null;
        }
    }

    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[RegistryService.TOKEN_LENGTH];
        random.nextBytes(bytes);

        return Base64.getEncoder().encodeToString(bytes);
    }

    public User getUserByToken(String token) {
        return this.authenticatedUsers.get(token);
    }

    public void insertUserToken(User user, String token) {
        this.authenticatedUsers.put(token, user);
    }

    public void removeUserToken(String token) {
        this.authenticatedUsers.remove(token);
    }

    public String authenticateUser(String username, String hashedPassword) {
        try {
            User user = this.hyperledgerService.login(username, hashedPassword);
            String token = this.generateToken();

            if (this.authenticatedUsers.containsValue(user)) {
                System.out.println("[Registry Service] User " + user.getUsername() + " is already authenticated.");
                return null;
            }

            this.insertUserToken(user, token);

            System.out.println("[Registry Service] Authenticated user " +
                    user.getUsername() + " successfully with token " + token);
            return token;
        } catch (IOException | ContractException e) {
            System.err.println("[Registry Service] Failed to login user: " + e.getMessage());
            return null;
        }
    }

    public UserRole authorizeUser(String userToken) {
        User user = this.getUserByToken(userToken);
        if (user != null) {
            System.out.println("[Registry Service] Authorized user " + user.getUsername() + " with token " + userToken);
            return user.getRole();
        }

        System.err.println("[Registry Service] Failed to find authenticated user with token " + userToken);
        return null;
    }

    public boolean logoutUser(String userToken) {
        User user = this.getUserByToken(userToken);
        if (user != null) {
            this.authenticatedUsers.remove(userToken);
            System.out.println("[Registry Service] Logged out user " + user.getUsername() + " successfully.");
            return true;
        } else {
            System.err.println("[Registry Service] Failed to logout user with token " +
                    userToken + ". It is not logged in.");
            return false;
        }
    }

    public boolean deleteUser(String authorToken, String targetUsername) {
        User user = this.getUserByToken(authorToken);
        if (user == null || !user.getRole().equals(UserRole.SUPERVISOR)) {
            System.err.println("[Registry Service] Failed to delete user " +
                    targetUsername + ". Insufficient permissions.");
            return false;
        }

        try {
            System.out.println("[Registry Service] Deleted user " + targetUsername + " successfully.");
            return this.hyperledgerService.deleteUser(targetUsername);
        } catch (IOException | ContractException | InterruptedException | TimeoutException e) {
            System.err.println("[Registry Service] Failed to delete user " + targetUsername + ": " + e.getMessage());
            return false;
        }
    }
}
