package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.SpecialConditionType;
import it.aruba.monitoring.model.StatusCsv;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialConditionService {
    private static final int UPSELL_YEARS = 3;

    private final NotificationService notificationService;
    private final ServiceRecordService serviceRecordService;

    public void evaluateSpecialCondition(List<ServiceRecord> recordList){
        recordList.forEach(this::evaluateSpecialCondition);
    }

    public void evaluateSpecialCondition(ServiceRecord record) {

        LocalDate today = LocalDate.now();

        if (isExpired(record, today)) {
            notificationService.notify(
                    SpecialConditionType.SERVICE_EXPIRED,
                    record
            );
        }

        if (isExpiringSoon(record, today)) {
            notificationService.notify(
                    SpecialConditionType.SERVICE_EXPIRING_SOON,
                    record
            );
        }

        if (isActiveButExpired(record, today)) {
            notificationService.notify(
                    SpecialConditionType.ACTIVE_BUT_EXPIRED,
                    record
            );
        }

        /*
        TODO
        Se un cliente ha più di 5 servizi scaduti, inviare un evento su broker (kafka, rabbit, pulsar, etc…)
        denominato `alerts.customer_expired`. L’evento sarà consumato da altro sistema esterno.*/
        if (hasMultipleExpiredServices(record, today)) {
            notificationService.notify(
                    SpecialConditionType.MULTIPLE_EXPIRED_SERVICES,
                    record
            );
        }

        /*
        TODO
        Se viene trovato un servizio attivo da oltre 3 anni,
        inviare una email al team marketing, segnalando l’opportunità di upselling
         */
        if (isActiveOverThreeYears(record, today)) {
            notificationService.notify(
                    SpecialConditionType.ACTIVE_OVER_THREE_YEARS,
                    record
            );
        }
    }

    private boolean isActiveButExpired(ServiceRecord record, LocalDate today) {
        return record.getStatus() == StatusCsv.ACTIVE
                && record.getExpirationDate().isBefore(today);
    }

    private boolean hasMultipleExpiredServices(ServiceRecord record, LocalDate today) {

        long expiredCount = serviceRecordService.countExpiredServices(
                record.getCustomerId(),
                today
        );
        return expiredCount >= 5;
    }

    private boolean isExpired(ServiceRecord record, LocalDate today) {
        return record.getExpirationDate().isBefore(today);
    }

    private boolean isExpiringSoon(ServiceRecord record, LocalDate today) {
        return record.getExpirationDate()
                .isBefore(today.plusDays(15));
    }

    private boolean isActiveOverThreeYears(ServiceRecord record, LocalDate today) {
        return record.getStatus() == StatusCsv.ACTIVE
                && record.getActivationDate()
                .isBefore(today.minusYears(UPSELL_YEARS));
    }

}
