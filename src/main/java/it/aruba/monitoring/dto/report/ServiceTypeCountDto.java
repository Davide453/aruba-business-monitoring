package it.aruba.monitoring.dto.report;


import it.aruba.monitoring.model.ServiceType;

public record ServiceTypeCountDto(
        ServiceType serviceType,
        Long total
) {}
