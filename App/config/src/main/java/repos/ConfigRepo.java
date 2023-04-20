package repos;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.TreeMap;

import static com.mongodb.client.model.Filters.eq;

public class ConfigRepo {
    private final String databaseAddress;
    private final Gson gson;
    private MongoClient mongoClient;

    public ConfigRepo() {
        this.databaseAddress = System.getenv("CONFIG_DATABASE_ADDRESS");
        this.gson = new Gson();
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

    public String getServiceConfig(String serviceId) {
        MongoDatabase mongoDatabase = this.newConnection();
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("configs");

        Document document = mongoCollection.find(eq("serviceId", serviceId)).first();
        this.mongoClient.close();

        return document != null ? document.getString("serviceConfig") : "{}";
    }

    public void setServiceConfig(String serviceId, String serviceConfig) {
        MongoDatabase mongoDatabase = this.newConnection();
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("configs");

        try {
            mongoCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("serviceId", serviceId)
                    .append("serviceConfig", serviceConfig)
            );

            this.mongoClient.close();
        } catch (MongoException e) {
            System.err.println("[Config Repo] Failed to update service configuration. Service: " +
                    serviceId + "; Config: " + serviceConfig);
        }

    }
}
