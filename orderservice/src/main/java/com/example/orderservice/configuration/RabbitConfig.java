package com.example.orderservice.configuration;

import com.example.orderservice.configuration.RabbitQueue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "amq.topic";

    // --- Exchange ---
    @Bean
    public TopicExchange topicExchange() { return new TopicExchange(EXCHANGE); }

    // --- Queue ---
    @Bean
    public Queue orderCreatedQueue() { return new Queue(RabbitQueue.ORDER_CREATED.getQueueName(), true); }
    @Bean
    public Queue orderUpdatedQueue() { return new Queue(RabbitQueue.ORDER_UPDATED.getQueueName(), true); }
    @Bean
    public Queue orderCanceledQueue() { return new Queue(RabbitQueue.ORDER_CANCELED.getQueueName(), true); }
    @Bean
    public Queue orderDeletedQueue() { return new Queue(RabbitQueue.ORDER_DELETED.getQueueName(), true); }

    // --- Binding ---
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(topicExchange).with(RabbitQueue.ORDER_CREATED.getRoutingKey());
    }
    @Bean
    public Binding orderUpdatedinding(Queue orderUpdatedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderUpdatedQueue).to(topicExchange).with(RabbitQueue.ORDER_UPDATED.getRoutingKey());
    }
    @Bean
    public Binding orderCanceledBinding(Queue orderCanceledQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderCanceledQueue).to(topicExchange).with(RabbitQueue.ORDER_CANCELED.getRoutingKey());
    }
    @Bean
    public Binding orderDeletedBinding(Queue orderDeletedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderDeletedQueue).to(topicExchange).with(RabbitQueue.ORDER_DELETED.getRoutingKey());
    }

    // --- JSON converter ---
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- ConnectionFactory ---
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        // enable publisher confirms and returns
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        factory.setPublisherReturns(true);

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("✅ Order transmitted to exchange.!");
            } else {
                System.out.println("❌ Order not transmitted! Cause: " + cause);
            }
        });

        template.setReturnsCallback(returned -> {
            System.out.println("⚠️ Message not directed to queue:");
            System.out.println("↳ ReplyText: " + returned.getReplyText());
            System.out.println("↳ RoutingKey: " + returned.getRoutingKey());
        });

        return template;
    }
}
