package services;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HyperledgerService {

    private final Wallet wallet;
    private final Genson genson;

    private final static String PROFILER_USER_ID = "profilerUser";
    private final static String PROFILER_CHANNEL = "profiles";
    private final static String PROFILER_CONTRACT = "profiler";

    public HyperledgerService() throws Exception {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        this.genson = new Genson();
    }

    public Contract getContract(Gateway gateway) {
        return gateway.getNetwork(PROFILER_CHANNEL).getContract(PROFILER_CONTRACT);
    }

    public Gateway getGateway() throws IOException {
        Path networkConfigPath = Paths.get( "resources", "org1.example.com", "connection-org1.json");
        return Gateway.createBuilder()
                .identity(this.wallet, PROFILER_USER_ID)
                .networkConfig(networkConfigPath)
                .discovery(true)
                .connect();
    }
}
