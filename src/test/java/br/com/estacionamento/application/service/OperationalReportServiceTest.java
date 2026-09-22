package br.com.estacionamento.application.service;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OperationalReportServiceTest {
    @Test
    void shouldAggregateFinishedStaysForTheRequestedPeriod() {
        ParkingStay first = finished("ABC1D23", "2026-09-10T10:00:00Z", "2026-09-10T11:30:00Z", "12.00");
        ParkingStay second = finished("DEF2G34", "2026-09-10T23:30:00Z", "2026-09-11T01:30:00Z", "16.00");
        OperationalReport report = new OperationalReportService(new FixedRepository(List.of(first, second)))
                .forPeriod(LocalDate.parse("2026-09-10"), LocalDate.parse("2026-09-10"));

        assertThat(report.entries()).isEqualTo(2);
        assertThat(report.exits()).isEqualTo(1);
        assertThat(report.revenue()).isEqualByComparingTo("12.00");
        assertThat(report.averageStayMinutes()).isEqualTo(90);
    }

    private static ParkingStay finished(String plate, String enteredAt, String exitedAt, String amount) {
        ParkingStay stay = ParkingStay.start(UUID.randomUUID(), LicensePlate.of(plate), Instant.parse(enteredAt));
        stay.finish(Instant.parse(exitedAt), new BigDecimal(amount));
        return stay;
    }
    private record FixedRepository(List<ParkingStay> stays) implements ParkingStayRepository {
        public ParkingStay save(ParkingStay stay) { return stay; }
        public Optional<ParkingStay> findActiveByPlate(LicensePlate plate) { return Optional.empty(); }
        public Optional<ParkingStay> findById(UUID id) { return stays.stream().filter(s -> s.id().equals(id)).findFirst(); }
        public List<ParkingStay> findAll(LicensePlate plate, StayStatus status) { return stays.stream().filter(s -> plate == null || s.licensePlate().equals(plate)).filter(s -> status == null || s.status() == status).toList(); }
        public List<ParkingStay> findAllActive() { return List.of(); }
        public long countActive() { return 0; }
    }
}
