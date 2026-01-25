package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.SpecialConditionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    public void notify(SpecialConditionType type, ServiceRecord record) {
        //todo implement
        log.info(
                "Special condition [{}] detected for customer {} (service: {})",
                type,
                record.getCustomerId(),
                record.getServiceType()
        );
    }

    private void notifyMarketing(ServiceRecord record) {
        log.info(
                "Upselling opportunity: customer {} has active service {} for over 3 years",
                record.getCustomerId(),
                record.getServiceType()
        );
    }
}
