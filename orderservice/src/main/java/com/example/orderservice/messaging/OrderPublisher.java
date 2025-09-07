package com.example.orderservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class OrderPublisher {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void publishOrderCreated(String orderData) {
        try {
            rabbitTemplate.convertAndSend("amq.topic", "order.created", orderData);

            System.out.println("✅ Sent order.created event: " + orderData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
