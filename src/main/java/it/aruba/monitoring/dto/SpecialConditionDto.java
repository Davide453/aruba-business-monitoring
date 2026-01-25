package it.aruba.monitoring.dto;

import it.aruba.monitoring.model.SpecialConditionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialConditionDto {

    private SpecialConditionType type;
    private String customerId;
    private String serviceType;
}