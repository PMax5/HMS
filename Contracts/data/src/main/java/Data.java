import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Contract(
        name = "data",
        info = @Info(
                title = "Data Contract",
                description = "HMS Data Contract",
                version = "1.0",
                contact = @Contact(
                        email = "servers@pedromax.pt",
                        name = "HMS"
                )
        )
)
@Default
public final class Data implements ContractInterface {

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void CreateDataLog(final Context ctx, final String dataLogString) {
        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(String.valueOf(UUID.randomUUID()), dataLogString);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetDataLogsForUser(final Context ctx, final String username) {
        ChaincodeStub stub = ctx.getStub();

        String query = String.format("{ \"selector\": { \"userId\": \"%s\" } }", username);
        QueryResultsIterator<KeyValue> results = stub.getQueryResult(query);

        List<DataLog> queryResults = new ArrayList<>();

        try {
            results.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (KeyValue result: results) {
            DataLog dataLog = genson.deserialize(result.getStringValue(), DataLog.class);
            queryResults.add(dataLog);
        }

        return genson.serialize(queryResults);
    }
}
