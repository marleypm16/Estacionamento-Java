package br.com.estacionamento.application.service;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.application.port.PlateDetectionRepository;
import br.com.estacionamento.domain.model.DetectionDirection;
import br.com.estacionamento.domain.model.DetectionStatus;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.PlateDetection;
import br.com.estacionamento.domain.model.StayStatus;
import br.com.estacionamento.domain.model.Tariff;
import br.com.estacionamento.domain.service.DefaultTariffCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlateDetectionServiceTest {
    private final Instant now = Instant.parse("2026-09-22T12:00:00Z");
    private InMemoryStays stays;
    private PlateDetectionService service;

    @BeforeEach
    void setUp() {
        stays = new InMemoryStays();
        ParkingService parkingService = new ParkingService(stays, new DefaultTariffCalculator(),
                new Tariff(new BigDecimal("8.00"), new BigDecimal("4.00"), 0), Clock.fixed(now, ZoneOffset.UTC), 10);
        service = new PlateDetectionService(new InMemoryDetections(), parkingService, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void shouldKeepLowConfidenceReadingPending() {
        PlateDetection detection = service.receive("abc1d23", .72, DetectionDirection.ENTRY, now);

        assertThat(detection.status()).isEqualTo(DetectionStatus.PENDING);
        assertThat(detection.reason()).contains("Confiança abaixo");
        assertThat(stays.countActive()).isZero();
    }

    @Test
    void shouldRegisterTheAttendantConfirmedPlate() {
        PlateDetection pending = service.receive("ABC1D2?", .72, DetectionDirection.ENTRY, now);

        PlateDetection confirmed = service.confirm(pending.id(), "ABC1D23");

        assertThat(confirmed.status()).isEqualTo(DetectionStatus.PROCESSED);
        assertThat(stays.findActiveByPlate(LicensePlate.of("ABC1D23"))).isPresent();
    }

    @Test
    void shouldNormalizeAndRegisterHighConfidenceEntry() {
        PlateDetection detection = service.receive("abc-1d23", .98, DetectionDirection.ENTRY, now);

        assertThat(detection.status()).isEqualTo(DetectionStatus.PROCESSED);
        assertThat(stays.findActiveByPlate(LicensePlate.of("ABC1D23"))).isPresent();
    }

    @Test
    void shouldKeepDuplicateEntryAndExitWithoutStayPending() {
        service.receive("ABC1D23", .98, DetectionDirection.ENTRY, now);

        PlateDetection duplicate = service.receive("ABC1D23", .98, DetectionDirection.ENTRY, now);
        PlateDetection missingExit = service.receive("DEF2G34", .98, DetectionDirection.EXIT, now);

        assertThat(duplicate.status()).isEqualTo(DetectionStatus.PENDING);
        assertThat(duplicate.reason()).contains("estadia ativa");
        assertThat(missingExit.status()).isEqualTo(DetectionStatus.PENDING);
        assertThat(missingExit.reason()).contains("Não há estadia ativa");
        assertThat(service.listPending()).hasSize(2);
    }

    @Test
    void shouldNotChargeAStayTwiceWhenTheExitEventIsRepeated() {
        service.receive("ABC1D23", .98, DetectionDirection.ENTRY, now);
        PlateDetection firstExit = service.receive("ABC1D23", .98, DetectionDirection.EXIT, now);
        PlateDetection repeatedExit = service.receive("ABC1D23", .98, DetectionDirection.EXIT, now);

        assertThat(firstExit.status()).isEqualTo(DetectionStatus.PROCESSED);
        assertThat(repeatedExit.status()).isEqualTo(DetectionStatus.PENDING);
        assertThat(stays.findAll(null, StayStatus.FINISHED)).hasSize(1);
    }

    private static final class InMemoryDetections implements PlateDetectionRepository {
        private final Map<UUID, PlateDetection> values = new LinkedHashMap<>();
        public PlateDetection save(PlateDetection detection) { values.put(detection.id(), detection); return detection; }
        public Optional<PlateDetection> findById(UUID id) { return Optional.ofNullable(values.get(id)); }
        public List<PlateDetection> findAllByStatus(DetectionStatus status) {
            return values.values().stream().filter(d -> d.status() == status).sorted(Comparator.comparing(PlateDetection::detectedAt)).toList();
        }
    }

    private static final class InMemoryStays implements ParkingStayRepository {
        private final Map<UUID, ParkingStay> values = new LinkedHashMap<>();
        public ParkingStay save(ParkingStay stay) { values.put(stay.id(), stay); return stay; }
        public Optional<ParkingStay> findActiveByPlate(LicensePlate plate) { return values.values().stream().filter(s -> s.status() == StayStatus.ACTIVE && s.licensePlate().equals(plate)).findFirst(); }
        public Optional<ParkingStay> findById(UUID id) { return Optional.ofNullable(values.get(id)); }
        public List<ParkingStay> findAll(LicensePlate plate, StayStatus status) { return values.values().stream().filter(s -> plate == null || s.licensePlate().equals(plate)).filter(s -> status == null || s.status() == status).toList(); }
        public List<ParkingStay> findAllActive() { return findAll(null, StayStatus.ACTIVE); }
        public long countActive() { return findAllActive().size(); }
    }
}
