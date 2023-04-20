package models;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RpcClient extends com.rabbitmq.client.RpcClient {

    public RpcClient(RpcClientParams params) throws IOException {
        super(params);
    }

    public byte[] sendRequest(String queueName,
                              Channel channel,
                              Operations operationType,
                              byte[] requestBytes
    ) throws IOException, ExecutionException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();

        final Map<String, Object> headers = new TreeMap<>();
        headers.put("operationType", operationType.toString());

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .headers(headers)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", queueName, props, requestBytes);
        final CompletableFuture<byte[]> response = new CompletableFuture<>();

        String cTag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(delivery.getBody());
            }
        }, consumerTag -> {
        });

        byte[] result = response.get();
        channel.basicCancel(cTag);

        return result;
    }


}
