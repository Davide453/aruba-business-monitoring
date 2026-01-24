package it.aruba.monitoring.dto;

import it.aruba.monitoring.model.StatusCsv;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ServiceRecordDto {

    private String customerId;
    private String serviceType;

    private LocalDate activationDate;
    private LocalDate expirationDate;

    private BigDecimal amount;
    private StatusCsv status;
}
