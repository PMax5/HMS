import models.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import services.RegistryService;

import java.util.ArrayList;
import java.util.List;

public class RegisterUserTests {

    protected RegistryService registryService;

    @BeforeAll
    public void initService() {
        try {
            List<String> hyperledgerUserIds = new ArrayList<>();
            hyperledgerUserIds.add("dataUser");
            hyperledgerUserIds.add("profilerUser");

            Config config = new Config("org1.department1", "Org1MSP", hyperledgerUserIds);
            RegistryService registryService = new RegistryService(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
