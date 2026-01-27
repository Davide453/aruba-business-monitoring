package it.aruba.monitoring.service;

import it.aruba.monitoring.dto.report.AverageSpendPerCustomerDto;
import it.aruba.monitoring.dto.report.ReportSummaryDto;
import it.aruba.monitoring.dto.report.ServiceTypeCountDto;
import it.aruba.monitoring.repository.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRecordService {
    private final ServiceRecordRepository serviceRecordRepository;

    public long countExpiredServices(String customerId, LocalDate today) {
        return serviceRecordRepository.countExpiredServices(customerId, today);
    }

    public ReportSummaryDto getSummary() {

        ReportSummaryDto summaryDto = new ReportSummaryDto();

        List<ServiceTypeCountDto> activeServicesByType = serviceRecordRepository.countActiveServicesByType()
                .stream().map(e ->
                        new ServiceTypeCountDto(e.getServiceType(), e.getTotal())).toList();

        summaryDto.setActiveServicesByType(activeServicesByType);

        List<AverageSpendPerCustomerDto> averageSpendPerCustomerDto = serviceRecordRepository.calculateAvgSpendPerCustomer()
                .stream().map(e ->
                        new AverageSpendPerCustomerDto(e.getCustomerId(), e.getAverage())).toList();
        summaryDto.setAverageSpendPerCustomer(averageSpendPerCustomerDto);

        List<String> customersWithMultipleExpiredServices = serviceRecordRepository.findCustomersWithMultipleExpiredServices();
        summaryDto.setCustomersWithMultipleExpiredServices(customersWithMultipleExpiredServices);
        List<String> customersWithServicesExpiringSoon = serviceRecordRepository.findCustomersWithServicesExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(15));
        summaryDto.setCustomersWithServicesExpiringSoon(customersWithServicesExpiringSoon);

        return summaryDto;
    }
}

