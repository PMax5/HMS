package models;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class RpcServer extends com.rabbitmq.client.RpcServer {

    private final Map<Operations, DeliverCallback> operations = new TreeMap<>();

    public RpcServer(Channel channel, String queueName) throws IOException {
        super(channel, queueName);
    }

    public void addOperationHandler(Operations operationId, DeliverCallback deliverCallback) throws IOException {
        this.operations.put(operationId, deliverCallback);
    }

    public void sendResponseAndAck(Delivery delivery, byte[] responseBytes) throws IOException {
        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

        this.getChannel().basicPublish("", delivery.getProperties().getReplyTo(), replyProperties, responseBytes);
        this.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    public void executeOperationHandler(Delivery delivery) throws IOException {
        Operations operation = (Operations) delivery.getProperties().getHeaders().get("operationType");
        DeliverCallback operationHandler = this.operations.get(operation);
        this.getChannel().basicConsume(this.getQueueName(), false, operationHandler, (consumerTag -> {}));
    }
}
