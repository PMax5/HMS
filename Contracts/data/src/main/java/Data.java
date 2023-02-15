import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;


@Contract(
        name = "data",
        info = @Info(
                title = "Data Contract",
                description = "HMS Data Contract",
                version = "1.4",
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
    public void CreateDataLog(final Context ctx, final String logId, final String dataLogString) {
        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(logId, dataLogString);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public DataLog[] GetDataLogsForUser(final Context ctx, final String username) {
        ChaincodeStub stub = ctx.getStub();

        String query = String.format("{ \"selector\": { \"userId\": \"%s\" } }", username);
        QueryResultsIterator<KeyValue> results = stub.getQueryResult(query);

        List<DataLog> queryResults = new ArrayList<>();

        System.out.println("Fetching results...");
        for (KeyValue result: results) {
            DataLog dataLog = genson.deserialize(result.getStringValue(), DataLog.class);
            System.out.println("Result for user: " + dataLog.getUserId());
            queryResults.add(dataLog);
        }

        return queryResults.toArray(new DataLog[0]);
    }
}
