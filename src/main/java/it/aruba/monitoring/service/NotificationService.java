package it.aruba.monitoring.service;

import it.aruba.monitoring.config.RabbitMqConfig;
import it.aruba.monitoring.dto.SpecialConditionDto;
import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.SpecialConditionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static it.aruba.monitoring.model.SpecialConditionType.MULTIPLE_EXPIRED_SERVICES;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final RabbitTemplate rabbitTemplate;

    public void notify(SpecialConditionType type, ServiceRecord record) {
        SpecialConditionDto conditionDto = new SpecialConditionDto(
                type,
                record.getCustomerId(),
                record.getServiceType()
        );
        final String routingKey = resolveRoutingKey(type);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EVENTS_EXCHANGE,
                routingKey,
                conditionDto
        );

        log.info(
                "Published event [{}] for customer {} to {}",
                type,
                record.getCustomerId(), routingKey
        );
    }

    private String resolveRoutingKey(SpecialConditionType type) {

        return switch (type) {

            case SERVICE_EXPIRED,
                 SERVICE_EXPIRING_SOON,
                 ACTIVE_BUT_EXPIRED,
                 ACTIVE_OVER_THREE_YEARS -> RabbitMqConfig.SPECIAL_CONDITION_ROUTING_KEY;
            case MULTIPLE_EXPIRED_SERVICES -> RabbitMqConfig.CUSTOMER_EXPIRED_ROUTING_KEY;

            default -> throw new IllegalArgumentException(
                    "Unsupported special condition type: " + type
            );
        };
    }

    private void notifyMarketing(ServiceRecord record) {
        log.info(
                "Upselling opportunity: customer {} has active service {} for over 3 years",
                record.getCustomerId(),
                record.getServiceType()
        );
    }
}
