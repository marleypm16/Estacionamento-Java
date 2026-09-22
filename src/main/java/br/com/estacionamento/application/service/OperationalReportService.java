package br.com.estacionamento.application.service;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class OperationalReportService {
    private final ParkingStayRepository repository;

    public OperationalReportService(ParkingStayRepository repository) { this.repository = repository; }

    public OperationalReport forPeriod(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final");
        List<ParkingStay> finished = repository.findAll(null, StayStatus.FINISHED);
        long entries = finished.stream().filter(stay -> inPeriod(stay.enteredAt().atZone(ZoneOffset.UTC).toLocalDate(), from, to)).count();
        List<ParkingStay> exitsInPeriod = finished.stream()
                .filter(stay -> inPeriod(stay.exitedAt().atZone(ZoneOffset.UTC).toLocalDate(), from, to)).toList();
        BigDecimal revenue = exitsInPeriod.stream().map(ParkingStay::amountCharged).reduce(BigDecimal.ZERO, BigDecimal::add);
        long averageMinutes = exitsInPeriod.isEmpty() ? 0 : Math.round(exitsInPeriod.stream()
                .mapToLong(ParkingStay::durationMinutes).average().orElse(0));
        return new OperationalReport(from, to, entries, exitsInPeriod.size(), revenue, averageMinutes);
    }

    private boolean inPeriod(LocalDate value, LocalDate from, LocalDate to) {
        return !value.isBefore(from) && !value.isAfter(to);
    }
}
