package services;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import models.DataLog;
import org.hyperledger.fabric.gateway.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class HyperledgerService {

    private final Wallet wallet;
    private final Genson genson;

    private final static String DATA_USER_ID = "dataUser";
    private final static String DATA_CHANNEL = "auditlogs";
    private final static String DATA_CONTRACT = "data";

    public HyperledgerService() throws Exception {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        this.genson = new Genson();
    }

    public Contract getContract(Gateway gateway) {
        return gateway.getNetwork(DATA_CHANNEL).getContract(DATA_CONTRACT);
    }

    public Gateway getGateway() throws IOException {
        Path networkConfigPath = Paths.get( "resources", "org1.example.com", "connection-org1.json");
        return Gateway.createBuilder()
                .identity(this.wallet, DATA_USER_ID)
                .networkConfig(networkConfigPath)
                .discovery(true)
                .connect();
    }

    public DataLog submitUserData(String username, int routeId, int vehicleId, List<Integer> bpm,
                               List<Integer> drowsiness, List<Integer> speeds, List<Long> timestamps, String shiftId)
            throws IOException, ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);

        DataLog dataLog = new DataLog(
                username,
                routeId,
                vehicleId,
                bpm,
                drowsiness,
                speeds,
                timestamps,
                shiftId
        );

        contract.submitTransaction("CreateDataLog", String.valueOf(UUID.randomUUID()), this.genson.serialize(dataLog));
        gateway.close();

        return dataLog;
    }

    public List<DataLog> getLogsForUser(String username) throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway);

        byte[] result = contract.evaluateTransaction("GetDataLogsForUser", username);
        gateway.close();

        return this.genson.deserialize(result, new GenericType<List<DataLog>>() {});
    }
}
