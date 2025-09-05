package com.example.orderservice.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "amq.topic";
    public static final String ORDER_QUEUE = "order.created.queue";
    public static final String ROUTING_KEY = "order.created";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue("user.registered", true); // durable
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(userQueue).to(topicExchange).with("user.registered");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
