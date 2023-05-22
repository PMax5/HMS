import models.Config;
import services.DataService;

public class BaseTest {

    public DataService initService() {
        try {
            Config config = new Config("dataUser");
            config.setDatabaseAddress("localhost");
            DataService dataService = new DataService(config);
            dataService.loadHyperledgerService(config);

            return dataService;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
