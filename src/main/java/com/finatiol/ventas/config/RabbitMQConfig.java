package com.finatiol.ventas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE     = "finatiol.exchange";
    public static final String QUEUE        = "ventas.notificaciones.queue";
    public static final String ROUTING_KEY  = "venta.realizada";

    @Bean
    public TopicExchange finatiolExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue ventasNotificacionesQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding ventasBinding(Queue ventasNotificacionesQueue, TopicExchange finatiolExchange) {
        return BindingBuilder
                .bind(ventasNotificacionesQueue)
                .to(finatiolExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
