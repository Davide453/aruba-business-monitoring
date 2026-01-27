package it.aruba.monitoring.mapper;

import it.aruba.monitoring.dto.ServiceRecordRaw;
import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.ServiceType;
import it.aruba.monitoring.model.StatusCsv;
import org.hibernate.query.sqm.ParsingException;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "spring")
public interface ServiceRecordMapper {


    ServiceRecord toEntity(ServiceRecordRaw dto);


    default LocalDate mapDate(String value) {
        if (value == null || value.isBlank()) {
            throw new ParsingException("Date is missing");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ParsingException("Invalid date: " + value);
        }
    }

    default BigDecimal mapAmount(String value) {
        if (value == null || value.isBlank()) {
            throw new ParsingException("Amount is missing");
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new ParsingException("Invalid amount: " + value);
        }
    }

    default StatusCsv mapStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new ParsingException("Status is missing");
        }
        try {
            return StatusCsv.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ParsingException("Invalid status: " + value);
        }
    }

    default ServiceType mapServiceType(String value) {
        if (value == null || value.isBlank()) {
            throw new ParsingException("Service Type is missing");
        }
        try {
            return ServiceType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ParsingException("Invalid service type: " + value);
        }
    }
}
