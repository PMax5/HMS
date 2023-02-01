package services;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import models.Gender;
import models.UserRole;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class HyperledgerService {

    private final HFCAClient hfcaClient;
    private final Wallet wallet;
    private final Genson genson;

    private final static String ADMIN_USER_ID = "admin";
    private final static String REGISTRY_USER_ID = "admin";
    private final static String REGISTRY_CHANNEL = "userdata";
    private final static String REGISTRY_CONTRACT = "registry";

    public HyperledgerService() throws Exception {
        Properties properties = new Properties();
        properties.put("pemFile", "resources/org1.example.com/ca/ca.org1.example.com-cert.pem");
        properties.put("allowAllHostNames", "true");

        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.hfcaClient = HFCAClient.createNewInstance("https://localhost:7054", properties);
        this.hfcaClient.setCryptoSuite(CryptoSuiteFactory.getDefault().getCryptoSuite());
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        this.genson = new Genson();
    }

    public void enrollAdminUser() throws Exception {
        if (this.wallet.get(ADMIN_USER_ID) != null) {
            System.out.println("The admin user already exists. Skipping admin user creation...");
            return;
        }

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.addHost("localhost");
        enrollmentRequest.setProfile("tls");
        Enrollment enrollment = this.hfcaClient.enroll(ADMIN_USER_ID, "adminpw", enrollmentRequest);

        Identity adminUser = Identities.newX509Identity("Org1MSP", enrollment);
        this.wallet.put(ADMIN_USER_ID, adminUser);

        System.out.println("The admin user was successfully registered.");
    }

    public void registerServicesUsers(List<String> usernames, List<Set<String>> rolesSets,
                                      String affiliation, String mspId) throws Exception {
        X509Identity adminIdentity = (X509Identity) wallet.get(ADMIN_USER_ID);
        if (adminIdentity == null) {
            System.out.println("The admin user is not registered. Failed to register a new Hyperledger Fabric user.");
            return;
        }

        User adminUser = new User() {
            @Override
            public String getName() {
                return ADMIN_USER_ID;
            }

            @Override
            public Set<String> getRoles() {
                return null;
            }

            @Override
            public String getAccount() {
                return null;
            }

            @Override
            public String getAffiliation() {
                return affiliation;
            }

            @Override
            public Enrollment getEnrollment() {
                return new Enrollment() {
                    @Override
                    public PrivateKey getKey() {
                        return adminIdentity.getPrivateKey();
                    }

                    @Override
                    public String getCert() {
                        return Identities.toPemString(adminIdentity.getCertificate());
                    }
                };
            }

            @Override
            public String getMspId() {
                return mspId;
            }
        };

        for (String username : usernames) {
            RegistrationRequest registrationRequest = new RegistrationRequest(username);
            registrationRequest.setAffiliation(affiliation);
            registrationRequest.setEnrollmentID(username);
            String enrollmentSecret = this.hfcaClient.register(registrationRequest, adminUser);
            Enrollment enrollment = this.hfcaClient.enroll(username, enrollmentSecret);
            this.wallet.put(username, Identities.newX509Identity(mspId, enrollment));

            System.out.println("Successfully registered user " + username + " and enrolled them.");
        }
    }

    public Contract getContract(Gateway gateway) {
        return gateway.getNetwork(REGISTRY_CHANNEL).getContract(REGISTRY_CONTRACT);
    }

    public Gateway getGateway() throws IOException {
        Path networkConfigPath = Paths.get( "resources", "org1.example.com", "connection-org1.json");
        return Gateway.createBuilder()
                .identity(this.wallet, REGISTRY_USER_ID)
                .networkConfig(networkConfigPath)
                .discovery(true)
                .connect();
    }

    public models.User registerUser(String username, String name, int age, Gender gender,
                                    UserRole userRole, String hashedPassword)
            throws IOException, ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);
        byte[] user = contract.submitTransaction("createUser",
                username,
                name,
                String.valueOf(age),
                gender.toString(),
                userRole.toString(),
                hashedPassword
        );

        gateway.close();
        return this.genson.deserialize(user, models.User.class);
    }

    public models.User login(String username, String hashedPassword) throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);

        byte[] user = contract.evaluateTransaction("login", username, hashedPassword);

        gateway.close();
        return this.genson.deserialize(user, models.User.class);
    }

    public List<models.User> getAllUsers() throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);

        byte[] users = contract.evaluateTransaction("queryAllUsers");

        gateway.close();
        return this.genson.deserialize(users, new GenericType<List<models.User>>(){});
    }

    public boolean deleteUser(String username) throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);

        byte[] result = contract.evaluateTransaction("deleteUser", username);

        gateway.close();
        return Boolean.parseBoolean(new String(result));

    }
}
