package com.jellystudy.web.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AI_REQUEST_QUEUE = "ai.request.queue";
    public static final String AI_RESPONSE_QUEUE = "ai.response.queue";
    public static final String AI_EXCHANGE = "ai.exchange";
    public static final String AI_ROUTING_KEY = "ai.request";

    @Bean
    public Queue aiRequestQueue() {
        return new Queue(AI_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue aiResponseQueue() {
        return new Queue(AI_RESPONSE_QUEUE, true);
    }

    @Bean
    public TopicExchange aiExchange() {
        return new TopicExchange(AI_EXCHANGE);
    }

    @Bean
    public Binding bindingRequest() {
        return BindingBuilder.bind(aiRequestQueue())
                .to(aiExchange())
                .with(AI_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
