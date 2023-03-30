package services;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import models.Config;
import models.DataLog;
import models.Shift;
import models.ShiftLog;
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
    private final static String PROCESSED_DATA_CHANNEL = "shift_data";
    private final static String PROCESSED_DATA_CONTRACT = "shiftdata";
    private final Config config;

    public HyperledgerService(Config config) throws Exception {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        this.genson = new Genson();
        this.config = config;
    }

    public Contract getContract(Gateway gateway, String channel, String contract) {
        return gateway.getNetwork(channel).getContract(contract);
    }

    public Gateway getGateway() throws IOException {
        Path networkConfigPath = Paths.get( "resources", "org1.example.com", "connection-org1.json");
        return Gateway.createBuilder()
                .identity(this.wallet, this.config.getHyperledgerUserId())
                .networkConfig(networkConfigPath)
                .discovery(true)
                .connect();
    }

    public DataLog submitUserData(String username, int routeId, int vehicleId, List<Integer> bpm,
                               List<Integer> drowsiness, List<Integer> speeds, List<Long> timestamps, String shiftId)
            throws IOException, ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, DATA_CHANNEL, DATA_CONTRACT);

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
        Contract contract = this.getContract(gateway, DATA_CHANNEL, DATA_CONTRACT);

        byte[] result = contract.evaluateTransaction("GetDataLogsForUser", username);
        gateway.close();

        return this.genson.deserialize(result, new GenericType<List<DataLog>>() {});
    }

    public List<DataLog> getLogsForShift(String shiftId) throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, DATA_CHANNEL, DATA_CONTRACT);

        byte[] result = contract.evaluateTransaction("GetDataLogsForShift", shiftId);
        gateway.close();

        return this.genson.deserialize(result, new GenericType<List<DataLog>>() {});
    }

    public ShiftLog submitShiftLogData(String userId, String shiftId, int vehicleId, int routeId,
                                       int averageBPM, int averageDrowsiness, int averageSpeed)
            throws IOException, ContractException, InterruptedException, TimeoutException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, PROCESSED_DATA_CHANNEL, PROCESSED_DATA_CONTRACT);

        ShiftLog shiftLog = new ShiftLog(
                userId,
                shiftId,
                vehicleId,
                routeId,
                averageBPM,
                averageDrowsiness,
                averageSpeed
        );

        byte[] result = contract.submitTransaction(
                "SubmitShiftData",
                shiftLog.getShiftId(),
                this.genson.serialize(shiftLog)
        );
        gateway.close();

        return this.genson.deserialize(result, ShiftLog.class);
    }

    public List<ShiftLog> getShiftLogsForUser(String userId) throws IOException, ContractException {
        Gateway gateway = this.getGateway();
        Contract contract = this.getContract(gateway, PROCESSED_DATA_CHANNEL, PROCESSED_DATA_CONTRACT);

        byte[] result = contract.evaluateTransaction("GetShiftDataLogs", userId);
        gateway.close();

        return this.genson.deserialize(result, new GenericType<List<ShiftLog>>() {});
    }
}
