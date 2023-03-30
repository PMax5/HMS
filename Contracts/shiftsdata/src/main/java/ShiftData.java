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
        name = "shiftdata",
        info = @Info(
                title = "Shift Data Contract",
                description = "HMS shift data contract",
                version = "1.0",
                contact = @Contact(
                        email = "servers@pedromax.pt",
                        name = "HMS"
                )
        )
)
@Default
public final class ShiftData implements ContractInterface {

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void SubmitShiftData(final Context ctx, final String shiftId, final String shiftString) {
        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(shiftId, shiftString);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public ShiftDataLog[] GetShiftDataLogs(final Context ctx, final String userId) {
        ChaincodeStub stub = ctx.getStub();

        String query = String.format("{ \"selector\": { \"userId\": \"%s\" } }", userId);
        QueryResultsIterator<KeyValue> results = stub.getQueryResult(query);

        List<ShiftDataLog> queryResults = new ArrayList<>();

        System.out.println("Fetching results...");
        for (KeyValue result: results) {
            ShiftDataLog shiftDataLog = genson.deserialize(result.getStringValue(), ShiftDataLog.class);
            System.out.println("Result for shift log: " + shiftDataLog.getShiftId());
            queryResults.add(shiftDataLog);
        }

        return queryResults.toArray(new ShiftDataLog[0]);
    }
}
