package services;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class HyperledgerService {

    private final HFCAClient hfcaClient;
    private final Wallet wallet;

    private final static String ADMIN_USER_ID = "admin";

    public HyperledgerService() throws Exception {
        Properties properties = new Properties();
        properties.put("pemFile", "../properties/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem");
        properties.put("allowAllHostNames", "true");

        this.hfcaClient = HFCAClient.createNewInstance("https://localhost:7054", properties);
        this.hfcaClient.setCryptoSuite(CryptoSuiteFactory.getDefault().getCryptoSuite());
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
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

        final int length = usernames.size();
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

        for (int i = 0; i < length; i++) {
            RegistrationRequest registrationRequest = new RegistrationRequest(usernames.get(i));
            registrationRequest.setAffiliation(affiliation);
            registrationRequest.setEnrollmentID(usernames.get(i));
            String enrollmentSecret = this.hfcaClient.register(registrationRequest, adminUser);
            Enrollment enrollment = this.hfcaClient.enroll(usernames.get(i), enrollmentSecret);
            this.wallet.put(usernames.get(i), Identities.newX509Identity(mspId, enrollment));

            System.out.println("Successfully registered user " + usernames.get(i) + " and enrolled them.");
        }

    }
}
