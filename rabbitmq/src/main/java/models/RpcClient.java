package models;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.RpcClientParams;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RpcClient extends com.rabbitmq.client.RpcClient {

    private Map<Operations, DeliverCallback> operations = new TreeMap<>();
    private final String queueName;

    public RpcClient(RpcClientParams params, String queueName) throws IOException {
        super(params);

        this.queueName = queueName;
    }

    public void addOperationReplyHandler(Operations operationId, DeliverCallback deliverCallback) throws IOException {
        this.operations.put(operationId, deliverCallback);
    }

    public byte[] sendRequest(String configQueueName, Channel channel, byte[] requestBytes) throws IOException, ExecutionException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
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
