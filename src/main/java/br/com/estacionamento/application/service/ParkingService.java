package br.com.estacionamento.application.service;

import br.com.estacionamento.application.exception.ActiveStayAlreadyExistsException;
import br.com.estacionamento.application.exception.ActiveStayNotFoundException;
import br.com.estacionamento.application.exception.ParkingLotFullException;
import br.com.estacionamento.application.exception.StayNotFoundException;
import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;
import br.com.estacionamento.domain.model.Tariff;
import br.com.estacionamento.domain.service.TariffCalculator;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ParkingService {

    private final ParkingStayRepository repository;
    private final TariffCalculator tariffCalculator;
    private final Tariff tariff;
    private final Clock clock;
    private final int capacity;

    public ParkingService(
            ParkingStayRepository repository,
            TariffCalculator tariffCalculator,
            Tariff tariff,
            Clock clock,
            int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser positiva");
        }
        this.repository = repository;
        this.tariffCalculator = tariffCalculator;
        this.tariff = tariff;
        this.clock = clock;
        this.capacity = capacity;
    }

    @Transactional
    public ParkingStay registerEntry(String rawPlate) {
        LicensePlate plate = LicensePlate.of(rawPlate);
        if (repository.findActiveByPlate(plate).isPresent()) {
            throw new ActiveStayAlreadyExistsException(plate.value());
        }
        if (repository.countActive() >= capacity) {
            throw new ParkingLotFullException();
        }

        ParkingStay stay = ParkingStay.start(UUID.randomUUID(), plate, clock.instant());
        return repository.save(stay);
    }

    @Transactional
    public ParkingStay registerExit(String rawPlate) {
        LicensePlate plate = LicensePlate.of(rawPlate);
        ParkingStay stay = repository.findActiveByPlate(plate)
                .orElseThrow(() -> new ActiveStayNotFoundException(plate.value()));

        Instant exitTime = clock.instant();
        BigDecimal amount = tariffCalculator.calculate(stay.enteredAt(), exitTime, tariff);
        stay.finish(exitTime, amount);
        return repository.save(stay);
    }

    @Transactional(readOnly = true)
    public ParkingStay findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new StayNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ParkingStay> search(String rawPlate, StayStatus status) {
        LicensePlate plate = rawPlate == null || rawPlate.isBlank() ? null : LicensePlate.of(rawPlate);
        return repository.findAll(plate, status);
    }

    @Transactional(readOnly = true)
    public List<ParkingStay> listActive() {
        return repository.findAllActive();
    }

    @Transactional(readOnly = true)
    public Occupancy getOccupancy() {
        long occupied = repository.countActive();
        return new Occupancy(capacity, occupied, Math.max(capacity - occupied, 0));
    }
}
