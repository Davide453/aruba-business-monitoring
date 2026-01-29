package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MarketingMailWorker {

    @RabbitListener(queues = "marketing.mail.queue")
    public void sendUpsellingMail(ServiceRecord event) {

        log.info(
            "[EMAIL] Sent upselling email to marketing team for customer {} (service {})",
            event.getCustomerId(),
            event.getServiceType()
        );
    }
}
