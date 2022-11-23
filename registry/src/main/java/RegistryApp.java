import com.rabbitmq.client.Channel;
import services.ConfigService;
import services.RabbitMqService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RegistryApp {
    public static void main(String[] args) {
        // Load RabbitMQ config
        ConfigService configService = new ConfigService();
        RabbitMqService rabbitMqService = new RabbitMqService();

        try {

            Channel channel = rabbitMqService.createNewChannel();

        } catch (IOException | TimeoutException e) {
            // TODO: Create specific exceptions and logger.
            e.printStackTrace();
        }
    }
}