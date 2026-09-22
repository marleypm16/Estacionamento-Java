package br.com.estacionamento.interfaces.rest.dto;

import br.com.estacionamento.application.service.OperationalReport;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OperationalReportResponse(LocalDate from, LocalDate to, long entries, long exits,
                                        BigDecimal revenue, long averageStayMinutes) {
    public static OperationalReportResponse from(OperationalReport report) {
        return new OperationalReportResponse(report.from(), report.to(), report.entries(), report.exits(), report.revenue(), report.averageStayMinutes());
    }
}
