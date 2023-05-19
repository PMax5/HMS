import models.Config;
import services.RegistryService;

import java.util.ArrayList;
import java.util.List;

public class RegistryBase {

    public RegistryService initService() {
        try {
            List<String> hyperledgerUserIds = new ArrayList<>();
            hyperledgerUserIds.add("dataUser");
            hyperledgerUserIds.add("profilerUser");

            Config config = new Config("org1.department1", "Org1MSP", hyperledgerUserIds);
            return new RegistryService(config);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
