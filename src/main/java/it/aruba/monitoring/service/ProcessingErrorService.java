package it.aruba.monitoring.service;

import it.aruba.monitoring.model.ProcessingError;
import it.aruba.monitoring.model.ProcessingErrorType;
import it.aruba.monitoring.repository.ProcessingErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingErrorService {
    private final ProcessingErrorRepository errorRepository;

    public void handleError(
            int rowNumber,
            String[] row,
            ProcessingErrorType type,
            Exception ex) {

        log.warn("CSV row {} rejected [{}]: {}", rowNumber, type, ex.getMessage());

        ProcessingError error = new ProcessingError();
        error.setRowNumber(rowNumber);
        error.setErrorType(type);
        error.setMessage(ex.getMessage());
        error.setRawRow(String.join(",", row));

        errorRepository.save(error);
    }

}
