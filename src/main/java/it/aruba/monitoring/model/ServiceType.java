package it.aruba.monitoring.model;

public enum ServiceType {
    HOSTING,
    PEC,
    SPID,
    FATTURAZIONE;

    public static ServiceType fromCsv(String value) {
        return ServiceType.valueOf(value.trim().toUpperCase());
    }
}
