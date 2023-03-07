package services;

import repos.ConfigRepo;

public class ConfigService {

    private final ConfigRepo configRepo;

    public ConfigService() {
        this.configRepo = new ConfigRepo();
    }

    public void updateConfig(String serviceId, String serviceConfig) {
        this.configRepo.setServiceConfig(serviceId, serviceConfig);
    }

    public String getConfig(String serviceId) {
        return this.configRepo.getServiceConfig(serviceId);
    }
}
