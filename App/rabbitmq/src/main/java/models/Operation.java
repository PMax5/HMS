package models;

import com.rabbitmq.client.Delivery;

import java.io.IOException;

public abstract class Operation {
    public abstract void execute(String consumerTag, Delivery delivery) throws IOException;
}