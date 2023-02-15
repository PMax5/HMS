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
        name = "registry",
        info = @Info(
            title = "Registry Contract",
            description = "HMS UserRegistry contract",
            version = "1.0",
            contact = @Contact(
                    email = "servers@pedromax.pt",
                    name = "HMS"
            )
        )
)
@Default
public final class Registry implements ContractInterface {

    private final Genson genson = new Genson();

    private enum UserRegistryErrors {
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        CREDENTIALS_DONT_MATCH
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public User queryUser(final Context ctx, final String key) {
        String userState = ctx.getStub().getStringState(key);

        if (userState.isEmpty()) {
            throw new ChaincodeException("User " + key + " does not exist.", UserRegistryErrors.USER_NOT_FOUND.toString());
        }

        return genson.deserialize(userState, User.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User createUser(final Context ctx, final String username, final String name, final int age, 
                           final String gender, final String role, final String hashedPassword) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(username);

        if (!userState.isEmpty()) {
            throw new ChaincodeException("User " + username + " already exists.", UserRegistryErrors.USER_ALREADY_EXISTS.toString());
        }

        User user = new User(name, username, age, gender, role, hashedPassword);
        userState = genson.serialize(user);
        stub.putStringState(username, userState);

        return user;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAllUsers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", ""); // Fetch all users with empty ""
        List<User> queryResults = new ArrayList<>();

        try {
            results.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (KeyValue result: results) {
            User user = genson.deserialize(result.getStringValue(), User.class);
            queryResults.add(user);
        }

        return genson.serialize(queryResults);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public User login(final Context ctx, final String username, final String hashedPassword) {
        User user = this.queryUser(ctx, username);

        boolean valid = user.getUsername().equals(username) && user.getHashedPassword().equals(hashedPassword);
        if (!valid)
            throw new ChaincodeException("User " + username + " credentials do not match.", UserRegistryErrors.CREDENTIALS_DONT_MATCH.toString());

        return user;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User updateUserProfileId(final Context ctx, final String username, final int profileId) {
        User user = this.queryUser(ctx, username);
        user.setProfileId(profileId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User addRouteId(final Context ctx, final String username, final int newRouteId) {
        User user = this.queryUser(ctx, username);
        user.addRouteId(newRouteId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public User removeRouteId(final Context ctx, final String username, final int targetRouteId) {
        User user = this.queryUser(ctx, username);
        user.removeRouteId(targetRouteId);

        ctx.getStub().putStringState(username, genson.serialize(user));
        return user;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean deleteUser(final Context ctx, final String username) {
        User user = this.queryUser(ctx, username);

        ctx.getStub().delState(user.getUsername());
        return true;
    }
}
