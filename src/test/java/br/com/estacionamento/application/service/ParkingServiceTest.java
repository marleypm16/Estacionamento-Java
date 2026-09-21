package br.com.estacionamento.application.service;

import br.com.estacionamento.application.exception.ActiveStayAlreadyExistsException;
import br.com.estacionamento.application.exception.ActiveStayNotFoundException;
import br.com.estacionamento.application.exception.ParkingLotFullException;
import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;
import br.com.estacionamento.domain.model.Tariff;
import br.com.estacionamento.domain.service.DefaultTariffCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParkingServiceTest {

    private final Instant now = Instant.parse("2026-09-21T12:00:01Z");
    private InMemoryParkingStayRepository repository;
    private ParkingService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingStayRepository();
        service = new ParkingService(
                repository,
                new DefaultTariffCalculator(),
                new Tariff(new BigDecimal("8.00"), new BigDecimal("4.00"), 0),
                Clock.fixed(now, ZoneOffset.UTC),
                2);
    }

    @Test
    void shouldRegisterNormalizedEntryUsingServerClock() {
        ParkingStay stay = service.registerEntry("abc-1234");

        assertThat(stay.licensePlate().value()).isEqualTo("ABC1234");
        assertThat(stay.enteredAt()).isEqualTo(now);
        assertThat(stay.status()).isEqualTo(StayStatus.ACTIVE);
    }

    @Test
    void shouldRejectDuplicateActiveStay() {
        service.registerEntry("ABC1D23");

        assertThatThrownBy(() -> service.registerEntry("abc1d23"))
                .isInstanceOf(ActiveStayAlreadyExistsException.class);
    }

    @Test
    void shouldRejectEntryWhenParkingLotIsFull() {
        service.registerEntry("ABC1D23");
        service.registerEntry("DEF2G34");

        assertThatThrownBy(() -> service.registerEntry("HIJ3K45"))
                .isInstanceOf(ParkingLotFullException.class);
    }

    @Test
    void shouldFinishActiveStayAndCalculateAmount() {
        ParkingStay activeStay = ParkingStay.start(
                UUID.randomUUID(), LicensePlate.of("ABC1D23"), now.minusSeconds(7_201));
        repository.save(activeStay);

        ParkingStay finished = service.registerExit("abc1d23");

        assertThat(finished.status()).isEqualTo(StayStatus.FINISHED);
        assertThat(finished.exitedAt()).isEqualTo(now);
        assertThat(finished.amountCharged()).isEqualByComparingTo("16.00");
    }

    @Test
    void shouldRejectExitWithoutActiveStay() {
        assertThatThrownBy(() -> service.registerExit("ABC1D23"))
                .isInstanceOf(ActiveStayNotFoundException.class);
    }

    private static final class InMemoryParkingStayRepository implements ParkingStayRepository {
        private final Map<UUID, ParkingStay> stays = new LinkedHashMap<>();

        @Override
        public ParkingStay save(ParkingStay stay) {
            stays.put(stay.id(), stay);
            return stay;
        }

        @Override
        public Optional<ParkingStay> findActiveByPlate(LicensePlate plate) {
            return stays.values().stream()
                    .filter(stay -> stay.status() == StayStatus.ACTIVE)
                    .filter(stay -> stay.licensePlate().equals(plate))
                    .findFirst();
        }

        @Override
        public Optional<ParkingStay> findById(UUID id) {
            return Optional.ofNullable(stays.get(id));
        }

        @Override
        public List<ParkingStay> findAll(LicensePlate plate, StayStatus status) {
            return stays.values().stream()
                    .filter(stay -> plate == null || stay.licensePlate().equals(plate))
                    .filter(stay -> status == null || stay.status() == status)
                    .toList();
        }

        @Override
        public List<ParkingStay> findAllActive() {
            return stays.values().stream().filter(stay -> stay.status() == StayStatus.ACTIVE).toList();
        }

        @Override
        public long countActive() {
            return stays.values().stream().filter(stay -> stay.status() == StayStatus.ACTIVE).count();
        }
    }
}
