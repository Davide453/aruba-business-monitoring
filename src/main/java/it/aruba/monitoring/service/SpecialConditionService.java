package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.SpecialConditionType;
import it.aruba.monitoring.model.StatusCsv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialConditionService {
    private static final int UPSELL_YEARS = 3;
    private static final int EXPIRING_SOON_DAYS = 15;
    private static final int MULTIPLE_EXPIRED_THRESHOLD = 5;

    private final NotificationService notificationService;
    private final ServiceRecordService serviceRecordService;

    public void evaluateSpecialCondition(List<ServiceRecord> recordList) {
        recordList.forEach(this::evaluateSpecialCondition);

        evaluateSpecialConditionByCustomer(recordList);
    }

    private void evaluateSpecialConditionByCustomer(List<ServiceRecord> recordList) {
        Map<String, List<ServiceRecord>> byCustomer = recordList.stream()
                .collect(Collectors.groupingBy(ServiceRecord::getCustomerId));
        byCustomer.forEach((customerId, customerRecords) -> {
            long expiredCount = serviceRecordService.countExpiredServices(customerId, LocalDate.now());
            customerRecords.forEach(record -> {
               /*
                Se un cliente ha più di 5 servizi scaduti, inviare un evento su broker (kafka, rabbit, pulsar, etc…)
                denominato `alerts.customer_expired`. L’evento sarà consumato da altro sistema esterno.*/
                if (expiredCount >= MULTIPLE_EXPIRED_THRESHOLD) {
                    notificationService.notify(
                            SpecialConditionType.MULTIPLE_EXPIRED_SERVICES,
                            customerRecords.get(0)
                    );
                }

            });
        });
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
        Se viene trovato un servizio attivo da oltre 3 anni,
        inviare una email al team marketing, segnalando l’opportunità di upselling
         */
        if (isActiveOverThreeYears(record, today)) {
            notificationService.notifyMarketing(
                    record
            );
        }
    }

    private boolean isActiveButExpired(ServiceRecord record, LocalDate today) {
        return record.getStatus() == StatusCsv.ACTIVE
                && record.getExpirationDate().isBefore(today);
    }



    private boolean isExpired(ServiceRecord record, LocalDate today) {
        return record.getExpirationDate().isBefore(today);
    }

    private boolean isExpiringSoon(ServiceRecord record, LocalDate today) {
        return record.getExpirationDate()
                .isBefore(today.plusDays(EXPIRING_SOON_DAYS)) && record.getStatus().equals(StatusCsv.ACTIVE);
    }

    private boolean isActiveOverThreeYears(ServiceRecord record, LocalDate today) {
        return record.getStatus() == StatusCsv.ACTIVE
                && record.getActivationDate()
                .isBefore(today.minusYears(UPSELL_YEARS));
    }

}
