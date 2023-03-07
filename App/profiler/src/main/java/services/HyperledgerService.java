package services;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import models.Gender;
import models.Profile;
import models.ShiftType;
import org.hyperledger.fabric.gateway.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class HyperledgerService {

    private final Wallet wallet;
    private final Genson genson;

    private final static String PROFILER_USER_ID = "profilerUser";
    private final static String PROFILER_CHANNEL = "profiles";
    private final static String PROFILER_CONTRACT = "profiler";
    private final static String REGISTRY_CONTRACT = "registry";

    public HyperledgerService() throws Exception {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        this.genson = new Genson();
    }

    public Contract getContract(Gateway gateway, String contract) {
        return gateway.getNetwork(PROFILER_CHANNEL).getContract(contract);
    }

    public Gateway getGateway() throws IOException {
        Path networkConfigPath = Paths.get( "resources", "org1.example.com", "connection-org1.json");
        return Gateway.createBuilder()
                .identity(this.wallet, PROFILER_USER_ID)
                .networkConfig(networkConfigPath)
                .discovery(true)
                .connect();
    }

    public Profile registerProfile(int minAge, int maxAge, String gender, int minShiftHours, int maxShiftHours,
                                   List<String> shiftTypes, List<Integer> routeIds) throws IOException,
            ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, PROFILER_CONTRACT);

        List<Integer> ageRange = new ArrayList<>();
        ageRange.add(minAge);
        ageRange.add(maxAge);

        List<Integer> shiftHoursRange = new ArrayList<>();
        shiftHoursRange.add(minShiftHours);
        shiftHoursRange.add(maxShiftHours);

        String uuid = String.valueOf(UUID.randomUUID());

        Profile profile = new Profile(
                uuid,
                ageRange,
                gender,
                shiftHoursRange,
                shiftTypes,
                routeIds
        );

        contract.submitTransaction("CreateProfile", uuid, this.genson.serialize(profile));
        gateway.close();

        return profile;
    }

    public List<Profile> getProfiles() throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, PROFILER_CONTRACT);

        byte[] result = contract.evaluateTransaction("GetProfiles");
        gateway.close();

        return this.genson.deserialize(result, new GenericType<List<Profile>>() {});
    }

    public void setProfile(String username, String profileId) throws IOException,
            ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, REGISTRY_CONTRACT);

        contract.submitTransaction("updateUserProfileId", username, profileId);
        gateway.close();
    }
}
