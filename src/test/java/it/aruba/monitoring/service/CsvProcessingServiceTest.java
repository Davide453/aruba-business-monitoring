package it.aruba.monitoring.service;

import it.aruba.monitoring.mapper.ServiceRecordMapper;
import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.ServiceType;
import it.aruba.monitoring.model.StatusCsv;
import it.aruba.monitoring.repository.ServiceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvProcessingServiceTest {

    @Mock
    private ServiceRecordMapper mapper;

    @Mock
    private ServiceRecordRepository serviceRecordRepository;

    @Mock
    private ProcessingErrorService processingErrorService;

    @Mock
    private SpecialConditionService specialConditionService;

    @InjectMocks
    private CsvProcessingService csvProcessingService;

    @Test
    void shouldProcessValidCsv() throws IOException {
        // Given
        String csvContent = """
                customer_id,service_type,activation_date,expiration_date,amount,status
                CUST001,hosting,2020-01-01,2025-12-31,99.99,active
                CUST002,pec,2021-01-01,2026-01-01,49.99,active
                """;

        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        ServiceRecord record1 = createMockRecord("CUST001");
        ServiceRecord record2 = createMockRecord("CUST002");

        when(mapper.toEntity(any())).thenReturn(record1, record2);
        when(serviceRecordRepository.saveAll(anyList()))
                .thenReturn(List.of(record1, record2));

        // When
        csvProcessingService.process(file);

        // Then
        verify(serviceRecordRepository, atLeastOnce()).saveAll(anyList());
        verify(specialConditionService, atLeastOnce()).evaluateSpecialCondition(anyList());
        verify(processingErrorService, never()).handleError(anyInt(), any(), any(), any());
    }

    @Test
    void shouldHandleInvalidRows() throws IOException {
        // Given
        String csvContent = """
                customer_id,service_type,activation_date,expiration_date,amount,status
                INVALID
                """;

        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        csvProcessingService.process(file);

        // Then
        verify(processingErrorService).handleError(anyInt(), any(), any(), any());
        verify(serviceRecordRepository, never()).saveAll(anyList());
    }

    private ServiceRecord createMockRecord(String customerId) {
        ServiceRecord record = new ServiceRecord();
        record.setCustomerId(customerId);
        record.setServiceType(ServiceType.HOSTING);
        record.setActivationDate(LocalDate.of(2020, 1, 1));
        record.setExpirationDate(LocalDate.of(2025, 12, 31));
        record.setAmount(BigDecimal.valueOf(99.99));
        record.setStatus(StatusCsv.ACTIVE);
        return record;
    }
}
