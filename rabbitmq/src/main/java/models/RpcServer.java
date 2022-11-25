package models;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class RpcServer extends com.rabbitmq.client.RpcServer {

    private Map<Operations, DeliverCallback> operations = new TreeMap<>();

    public RpcServer(Channel channel, String queueName) throws IOException {
        super(channel, queueName);
    }

    public void addOperation(Operations operationId, DeliverCallback deliverCallback) {
        this.operations.put(operationId, deliverCallback);
    }
}
