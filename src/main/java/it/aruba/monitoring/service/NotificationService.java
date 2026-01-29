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

        SpecialConditionDto dto = new SpecialConditionDto(
                type,
                record.getCustomerId(),
                record.getServiceType()
        );

        // enqueue su rabbit work queue
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_QUEUE,
                dto
        );

        log.info(
                "Enqueued notification [{}] for customer {}",
                type,
                record.getCustomerId()
        );
    }

    public void notifyMarketing(ServiceRecord record) {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EVENTS_EXCHANGE,
                    RabbitMqConfig.UPSELLING_ROUTING_KEY,
                    record
            );
        log.info(
                "Upselling opportunity: customer {} has active service {} for over 3 years",
                record.getCustomerId(),
                record.getServiceType()
        );
    }
}
