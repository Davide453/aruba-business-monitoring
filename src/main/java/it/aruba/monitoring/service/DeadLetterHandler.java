package it.aruba.monitoring.service;

import it.aruba.monitoring.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeadLetterHandler {
    
    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_DLQ)
    public void handleDeadLetter(Message failedMessage) {
        log.error("Message sent to DLQ: {}",
            new String(failedMessage.getBody()));
        
        // TODO
    }
}