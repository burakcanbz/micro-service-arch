package com.example.orderservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OrderConsumer {

    @RabbitListener(queues = "user.registered")
    public void handleOrderCreated(Map<String, Object> userData) {
        System.out.println("📥 Received user from user.registered topic: " + userData);
    }
}
