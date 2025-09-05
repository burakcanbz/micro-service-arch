package com.example.orderservice.messaging;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OrderPublisher {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void publishOrderCreated(Map<String, Object> orderData) {
        rabbitTemplate.convertAndSend("amq.topic", "order.created", orderData);
        System.out.println("✅ Sent order.created event: " + orderData);
    }
}
