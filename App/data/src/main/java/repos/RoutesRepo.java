package repos;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.Route;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class RoutesRepo {
    private final String databaseAddress;
    private MongoClient mongoClient;

    public RoutesRepo() {
        this.databaseAddress = System.getenv("CONFIG_DATABASE_ADDRESS");
    }

    private MongoDatabase newConnection() {
        String uri = "mongodb://" + this.databaseAddress + "/?retryWrites=true&w=majority";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();


        this.mongoClient = MongoClients.create(settings);
        return this.mongoClient.getDatabase("hms");
    }

    public Route getRoute(int routeId) throws Exception {
        MongoDatabase mongoDatabase = this.newConnection();
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("routes");

        Document document = mongoCollection.find(eq("id", routeId)).first();
        this.mongoClient.close();

        if (document == null) {
            throw new Exception("[Routes Repo] Failed to get route with ID " + routeId);
        }

        return new Gson().fromJson(document.toJson(), Route.class);
    }
}
