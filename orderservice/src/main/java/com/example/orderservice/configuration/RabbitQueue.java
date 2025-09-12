package com.example.orderservice.configuration;

public enum RabbitQueue{
    ORDER_CREATED("order.craeted.queue", "order.created"),
    ORDER_UPDATED("order.updated.queue", "order.updated"),
    ORDER_CANCELED("oreder.canceled.queue", "order.canceled"),
    ORDER_DELETED("order.deleted.queue", "order.deleted");

    private final String queueName;
    private final String routingKey;

    RabbitQueue(String queueName, String routingKey){
        this.queueName = queueName;
        this.routingKey = routingKey;
    }

    public String getQueueName(){
        return queueName;
    }

    public String getRoutingKey(){
        return routingKey;
    }
}