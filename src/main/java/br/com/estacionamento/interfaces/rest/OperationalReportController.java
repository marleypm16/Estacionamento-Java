package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.service.OperationalReportService;
import br.com.estacionamento.interfaces.rest.dto.OperationalReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class OperationalReportController {
    private final OperationalReportService reportService;
    private final AdministrativeAccess administrativeAccess;
    public OperationalReportController(OperationalReportService reportService, AdministrativeAccess administrativeAccess) {
        this.reportService = reportService; this.administrativeAccess = administrativeAccess;
    }
    @GetMapping("/operations")
    public OperationalReportResponse operations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(name = "X-Admin-Token", required = false) String adminToken) {
        administrativeAccess.require(adminToken);
        return OperationalReportResponse.from(reportService.forPeriod(from, to));
    }
}
