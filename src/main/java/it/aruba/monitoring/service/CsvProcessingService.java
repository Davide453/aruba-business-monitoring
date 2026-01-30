package it.aruba.monitoring.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import it.aruba.monitoring.dto.ServiceRecordRaw;
import it.aruba.monitoring.mapper.ServiceRecordMapper;
import it.aruba.monitoring.model.ProcessingErrorType;
import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.repository.ServiceRecordRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sqm.ParsingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvProcessingService {
    private static final int BATCH_SIZE = 500;
    private final ServiceRecordMapper mapper;
    private final ServiceRecordRepository serviceRecordRepository;
    private final ProcessingErrorService processingErrorService;
    private final SpecialConditionService specialConditionService;

    public void process(MultipartFile csv) {
        List<ServiceRecord> batch = new ArrayList<>(BATCH_SIZE);
        int rowNumber = 0;

        // skip CSV header
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csv.getInputStream()))
                .withSkipLines(1)
                .build();) {

            String[] row;

            while ((row = reader.readNext()) != null) {
                rowNumber++;

                try {
                    ServiceRecord entity = this.parseAndMap(row);
                    this.validate(entity);
                    batch.add(entity);

                    if (batch.size() == BATCH_SIZE) {
                        List<ServiceRecord> saved = serviceRecordRepository.saveAll(batch);
                        specialConditionService.evaluateSpecialCondition(saved);
                        batch.clear();
                    }

                } catch (ValidationException ex) {
                    processingErrorService.handleError(rowNumber, row, ProcessingErrorType.VALIDATION, ex);
                } catch (ParsingException ex) {
                    processingErrorService.handleError(rowNumber, row, ProcessingErrorType.PARSING, ex);
                }
            }

            if (!batch.isEmpty()) {
                List<ServiceRecord> saved = serviceRecordRepository.saveAll(batch);
                specialConditionService.evaluateSpecialCondition(saved);
            }


        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
    }

    private ServiceRecord parseAndMap(String[] row) {
        ServiceRecordRaw dto = this.fromRow(row);
        return mapper.toEntity(dto);
    }

    public ServiceRecordRaw fromRow(String[] row) {

        if (row.length != 6) {
            throw new ParsingException(
                    "Invalid column count: expected 6, found " + row.length);
        }
        ServiceRecordRaw recordRaw = new ServiceRecordRaw();
        recordRaw.setCustomerId(row[0]);
        recordRaw.setServiceType(row[1]);
        recordRaw.setActivationDate(row[2]);
        recordRaw.setExpirationDate(row[3]);
        recordRaw.setAmount(row[4]);
        recordRaw.setStatus(row[5]);

        return recordRaw;
    }

    private void validate(ServiceRecord record) {
        if (record.getCustomerId() == null || record.getCustomerId().isBlank()) {
            throw new ValidationException("customer_id is blank");
        }

        if (record.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("amount must be positive");
        }

        if (record.getActivationDate().isAfter(record.getExpirationDate())) {
            throw new ValidationException("activation_date after expiration_date");
        }
    }

}
