package it.aruba.monitoring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    //special conditions
    public static final String EVENTS_EXCHANGE = "events.exchange";
    public static final String SPECIAL_CONDITION_ROUTING_KEY = "events.special_condition";
    public static final String CUSTOMER_EXPIRED_ROUTING_KEY = "alerts.customer_expired";

    // WORK QUEUE
    public static final String NOTIFICATION_QUEUE = "notification.outbox.queue";
    public static final  String UPSELLING_ROUTING_KEY = "events.upselling";
    public static final String MARKETING_MAIL_QUEUE = "marketing.mail.queue";


    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public DirectExchange eventsExchange() {
        return new DirectExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public Queue marketingMailQueue() {
        return new Queue(MARKETING_MAIL_QUEUE, true);
    }

    @Bean
    public Binding marketingMailBinding(Queue marketingMailQueue, DirectExchange eventsExchange) {
        return BindingBuilder
                .bind(marketingMailQueue)
                .to(eventsExchange)
                .with(UPSELLING_ROUTING_KEY);
    }


    @Bean
    public JacksonJsonMessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
