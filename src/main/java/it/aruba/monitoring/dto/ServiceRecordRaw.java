package it.aruba.monitoring.dto;


import lombok.Data;

@Data

public class ServiceRecordRaw {

    private String customerId;
    private String serviceType;
    private String activationDate;
    private String expirationDate;
    private String amount;
    private String status;

}
