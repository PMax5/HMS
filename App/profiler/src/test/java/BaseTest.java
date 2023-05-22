import models.Config;
import services.ProfilerService;

public class BaseTest {

    public ProfilerService initService() {
        try {
            Config config = new Config(
                    "profilerUser",
                    0,
                    120,
                    60,
                    3
            );

            config.setDatabaseAddress("localhost");
            ProfilerService profilerService = new ProfilerService(config);
            profilerService.loadHyperLedgerService(config);

            return profilerService;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
