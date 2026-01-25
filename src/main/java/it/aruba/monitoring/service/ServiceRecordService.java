package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.repository.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRecordService {
    private final ServiceRecordRepository serviceRecordRepository;

    public long countExpiredServices(String customerId, LocalDate today) {
        return  serviceRecordRepository.countExpiredServices(customerId,today);
    }
}

