import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "UserRegistry",
        info = @Info(
                title = "UserRegistry Contract",
                description = "HMS UserRegistry contract",
                version = "1.0",
                contact = @Contact(
                        email = "admin@hms.hms",
                        name = "HMS"
                )
        )
)
@Default
public final class UserRegistry {

    private final Genson genson = new Genson();

    private enum UserRegistryErrors {
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS
    }

    @Transaction()
    public User queryUser(final Context ctx, final String key) {
        String userState = ctx.getStub().getStringState(key);

        if (userState.isEmpty()) {
            throw new ChaincodeException("User " + key + " does not exist.", UserRegistryErrors.USER_NOT_FOUND.toString());
        }

        return genson.deserialize(userState, User.class);
    }

    @Transaction()
    public User createUser(final Context ctx, final String key, final String name, final String username, final int age,
                           final GENDER gender, final String hashedPassword) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        if (!userState.isEmpty()) {
            throw new ChaincodeException("User " + key + " already exists.", UserRegistryErrors.USER_ALREADY_EXISTS.toString());
        }

        User user = new User(name, username, age, gender, hashedPassword);
        userState = genson.serialize(user);
        stub.putStringState(key, userState);

        return user;
    }

    @Transaction()
    public List<String> queryAllUsers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", ""); // Fetch all users with empty ""
        List<String> queryResults = new ArrayList<>();

        for (KeyValue result: results) {
            queryResults.add(result.getStringValue());
        }

        return queryResults;
    }

    @Transaction()
    public Boolean login(final Context ctx, final String username, final String hashedPassword) {
        User user = this.queryUser(ctx, username);

        return user.getUsername().equals(username) && user.getHashedPassword().equals(hashedPassword);
    }

    @Transaction()
    public User updateUserProfileId(final Context ctx, final String username, final int profileId) {
        User user = this.queryUser(ctx, username);
        user.setProfileId(profileId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }

    @Transaction()
    public User addRouteId(final Context ctx, final String username, final int newRouteId) {
        User user = this.queryUser(ctx, username);
        user.addRouteId(newRouteId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }

    @Transaction()
    public User removeRouteId(final Context ctx, final String username, final int targetRouteId) {
        User user = this.queryUser(ctx, username);
        user.removeRouteId(targetRouteId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }


}
