package it.aruba.monitoring.controller;

import it.aruba.monitoring.service.CsvProcessingService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final CsvProcessingService csvProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(@RequestParam("csv") MultipartFile csv) {
        String filename = csv.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "File must have .csv extension"
            );
        }
        if (csv.isEmpty()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Uploaded file is empty"
            );
        }

        csvProcessingService.process(csv);
        return ResponseEntity.accepted().build();
    }
}
