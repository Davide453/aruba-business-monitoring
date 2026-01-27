package it.aruba.monitoring.dto.report;

import java.math.BigDecimal;

public record AverageSpendPerCustomerDto(
        String customerId,
        BigDecimal averageAmount
) {
}
