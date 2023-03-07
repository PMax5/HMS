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
        name = "profiler",
        info = @Info(
                title = "Profiler Contract",
                description = "HMS Profiler contract",
                version = "1.0",
                contact = @Contact(
                        email = "servers@pedromax.pt",
                        name = "HMS"
                )
        )
)
@Default
public final class Profiler implements ContractInterface {

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void CreateProfile(final Context ctx, final String profileId, final String profileString) {
        ChaincodeStub stub = ctx.getStub();
        stub.putStringState(profileId, profileString);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Profile[] GetProfiles(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        List<Profile> queryResults = new ArrayList<>();

        System.out.println("Fetching results...");
        for (KeyValue result: results) {
            Profile profile = genson.deserialize(result.getStringValue(), Profile.class);
            System.out.println("Result for profile: " + profile.getId());
            queryResults.add(profile);
        }

        return queryResults.toArray(new Profile[0]);
    }
}
