package it.aruba.monitoring.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_error")
@Data
public class ProcessingError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rowNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingErrorType errorType;

    @Column(nullable = false)
    private String message;


    @Column(nullable = false)
    private String rawRow;
    @Column(nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

}
