package repos;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class RoutesRepo {
    private final String databaseAddress;
    private MongoClient mongoClient;

    public RoutesRepo() {
        this.databaseAddress = System.getenv("CONFIG_DATABASE_ADDRESS");
    }

    private MongoDatabase newConnection(String database) {
        String uri = "mongodb://" + this.databaseAddress + "/?retryWrites=true&w=majority";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();


        this.mongoClient = MongoClients.create(settings);
        return this.mongoClient.getDatabase(database);
    }

    public String getRoutes(String serviceId) {
        MongoDatabase mongoDatabase = this.newConnection("routes");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("configs");

        Document document = mongoCollection.find(eq("serviceId", serviceId)).first();
        this.mongoClient.close();

        return document != null ? document.getString("serviceConfig") : "{}";
    }
}
