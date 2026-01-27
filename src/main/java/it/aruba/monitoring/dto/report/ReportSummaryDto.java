package it.aruba.monitoring.dto.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportSummaryDto {

    private List<ServiceTypeCountDto> activeServicesByType;

    private List<AverageSpendPerCustomerDto> averageSpendPerCustomer;

    private List<String> customersWithMultipleExpiredServices;

    private List<String> customersWithServicesExpiringSoon;
}
