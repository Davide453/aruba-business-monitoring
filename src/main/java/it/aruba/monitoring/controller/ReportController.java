package it.aruba.monitoring.controller;


import it.aruba.monitoring.dto.report.ReportSummaryDto;
import it.aruba.monitoring.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/report")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ServiceRecordService recordService;

    @GetMapping("/summary")
    private ResponseEntity<ReportSummaryDto> getSummary() {
        ReportSummaryDto dto = recordService.getSummary();
        return ResponseEntity.ok(dto);
    }


}
