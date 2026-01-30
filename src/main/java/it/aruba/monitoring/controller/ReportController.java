package it.aruba.monitoring.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Return a summary of the data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary Report"),
    })
    private ResponseEntity<ReportSummaryDto> getSummary() {
        ReportSummaryDto dto = recordService.getSummary();
        return ResponseEntity.ok(dto);
    }


}
