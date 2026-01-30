package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.ServiceType;
import it.aruba.monitoring.model.SpecialConditionType;
import it.aruba.monitoring.model.StatusCsv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialConditionServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ServiceRecordService serviceRecordService;

    @InjectMocks
    private SpecialConditionService specialConditionService;

    @Test
    void shouldNotifyForServiceActiveOverThreeYears() {
        // Given
        ServiceRecord record = new ServiceRecord();
        record.setCustomerId("CUST001");
        record.setServiceType(ServiceType.HOSTING);
        record.setActivationDate(LocalDate.now().minusYears(4));
        record.setExpirationDate(LocalDate.now().plusYears(1));
        record.setAmount(BigDecimal.valueOf(99.99));
        record.setStatus(StatusCsv.ACTIVE);

        // When
        specialConditionService.evaluateSpecialCondition(record);

        // Then
        verify(notificationService).notifyMarketing(record);
    }

    @Test
    void shouldNotifyForMultipleExpiredServices() {
        // Given
        ServiceRecord record = new ServiceRecord();
        record.setCustomerId("CUST002");
        record.setServiceType(ServiceType.PEC);
        record.setActivationDate(LocalDate.now().minusYears(1));
        record.setExpirationDate(LocalDate.now().minusDays(10));
        record.setAmount(BigDecimal.valueOf(49.99));
        record.setStatus(StatusCsv.EXPIRED);

        when(serviceRecordService.countExpiredServices(eq("CUST002"), any()))
                .thenReturn(6L);

        // When
        specialConditionService.evaluateSpecialCondition(record);

        // Then
        verify(notificationService).notify(
                eq(SpecialConditionType.MULTIPLE_EXPIRED_SERVICES),
                eq(record)
        );
    }

    @Test
    void shouldNotNotifyWhenNoSpecialConditions() {
        // Given
        ServiceRecord record = new ServiceRecord();
        record.setCustomerId("CUST003");
        record.setServiceType(ServiceType.HOSTING);
        record.setActivationDate(LocalDate.now().minusMonths(6));
        record.setExpirationDate(LocalDate.now().plusMonths(6));
        record.setAmount(BigDecimal.valueOf(99.99));
        record.setStatus(StatusCsv.ACTIVE);

        when(serviceRecordService.countExpiredServices(any(), any()))
                .thenReturn(0L);

        // When
        specialConditionService.evaluateSpecialCondition(record);

        // Then
        verify(notificationService, never()).notify(any(), any());
        verify(notificationService, never()).notifyMarketing(any());
    }
}

