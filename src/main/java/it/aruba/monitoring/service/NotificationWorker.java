package it.aruba.monitoring.service;

import it.aruba.monitoring.config.RabbitMqConfig;
import it.aruba.monitoring.dto.SpecialConditionDto;
import it.aruba.monitoring.model.SpecialConditionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationWorker {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void process(SpecialConditionDto event) {

        try {
            String routingKey = resolveRoutingKey(event.getType());

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EVENTS_EXCHANGE,
                    routingKey,
                    event
            );

            log.info(
                    "Published event [{}] for customer {} to {}",
                    event.getType(),
                    event.getCustomerId(),
                    routingKey
            );

        } catch (Exception ex) {

            log.error(
                    "Error processing notification {}",
                    event,
                    ex
            );

            throw ex;
        }
    }

    private String resolveRoutingKey(SpecialConditionType type) {

        return switch (type) {
            case SERVICE_EXPIRED,
                 SERVICE_EXPIRING_SOON,
                 ACTIVE_BUT_EXPIRED,
                 ACTIVE_OVER_THREE_YEARS -> RabbitMqConfig.SPECIAL_CONDITION_ROUTING_KEY;

            case MULTIPLE_EXPIRED_SERVICES -> RabbitMqConfig.CUSTOMER_EXPIRED_ROUTING_KEY;
        };
    }
}
