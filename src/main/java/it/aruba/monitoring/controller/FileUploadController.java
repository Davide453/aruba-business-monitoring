package it.aruba.monitoring.controller;

import it.aruba.monitoring.service.CsvProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final CsvProcessingService csvProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(@RequestParam("csv") MultipartFile csv) {
        csvProcessingService.process(csv);
        return ResponseEntity.accepted().build();
    }
}
