package models;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RpcClient extends com.rabbitmq.client.RpcClient {

    private final Map<Operations, DeliverCallback> operations = new TreeMap<>();
    private final String queueName;

    public RpcClient(RpcClientParams params, String queueName) throws IOException {
        super(params);

        this.queueName = queueName;
    }

    public void addOperationReplyHandler(Operations operationId, DeliverCallback deliverCallback) throws IOException {
        this.operations.put(operationId, deliverCallback);
    }

    public void executeOperationReplyHandler(Operations operationId, Channel channel) throws IOException {
        DeliverCallback operationHandler = this.operations.get(operationId);
        channel.basicConsume(this.queueName, false, operationHandler, (consumerTag -> {}));
    }

    public byte[] sendRequest(String configQueueName,
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

        channel.basicPublish("", this.queueName, props, requestBytes);
        final CompletableFuture<byte[]> response = new CompletableFuture<>();

        String cTag = channel.basicConsume(configQueueName, true, (consumerTag, delivery) -> {
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
