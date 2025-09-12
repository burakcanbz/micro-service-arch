package com.example.orderservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.orderservice.logger.LoggerService;
import com.example.orderservice.configuration.RabbitConfig;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class OrderPublisher {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private LoggerService logger;

    public void publishOrderCreated(String orderData) {
        try {
            rabbitTemplate.convertAndSend("amq.topic", "order.created", orderData);

            System.out.println("✅ Sent order.created event: " + orderData);
        } catch (Exception e) {
            logger.error("Failed to sent order.craeted event: ", e);
        }
    }

    public void publishOrderUpdated(String orderData){
        try {
            rabbitTemplate.convertAndSend("amq.topic", "order.updated", orderData);
            logger.info("✅ Sent order.updated event: " + orderData);
        }
        catch (Exception e) {
            logger.error("Failed to sent order.updated event: ", e);
        }
    }

    public void publishOrderCanceled(String orderData) {
        try{
            rabbitTemplate.convertAndSend("amq.topic", "order.canceled", orderData);
            logger.info("✅ Sent order.updated event: " + orderData);
        }
        catch (Exception e) {
            logger.error("Failed to sent order.canceled event: ", e);
        }
    }

    public void publishOrderDeleted(String orderData){
        try {
            rabbitTemplate.convertAndSend("amq.topic", "order.deleted", orderData);
            logger.info("✅ Sent order.deleted event: " + orderData);
        }
        catch (Exception e){
            logger.error("Failed to sent order.deleted event: ", e);
        }
    }

}
