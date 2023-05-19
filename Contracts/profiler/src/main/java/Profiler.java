import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
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
    public Profile GetProfile(final Context ctx, final String key) {
        String profileState = ctx.getStub().getStringState(key);

        if (profileState.isEmpty()) {
            throw new ChaincodeException("Profile " + key + " does not exist.");
        }

        return genson.deserialize(profileState, Profile.class);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Profile QueryProfile(final Context ctx, final String key) {
        String profileState = ctx.getStub().getStringState(key);

        if (profileState.isEmpty()) {
            throw new ChaincodeException("Profile " + key + " does not exist.");
        }

        return genson.deserialize(profileState, Profile.class);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Profile[] GetProfiles(final Context ctx, final int type) {
        ChaincodeStub stub = ctx.getStub();
        String query = String.format("{ \"selector\": { \"type\": %d } }", type);
        QueryResultsIterator<KeyValue> results = stub.getQueryResult(query);

        List<Profile> queryResults = new ArrayList<>();

        System.out.println("Fetching results...");
        for (KeyValue result: results) {
            System.out.println(result.getStringValue());
            Profile profile = genson.deserialize(result.getStringValue(), Profile.class);
            System.out.println("Result for profile: " + profile.getId());
            queryResults.add(profile);
        }

        return queryResults.toArray(new Profile[0]);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteProfileById(final Context ctx, final String id) {
        Profile profile = this.QueryProfile(ctx, id);
        ctx.getStub().delState(profile.getId());
    }
}
