package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaParkingStayRepository implements ParkingStayRepository {

    private final SpringDataParkingStayRepository repository;

    public JpaParkingStayRepository(SpringDataParkingStayRepository repository) {
        this.repository = repository;
    }

    @Override
    public ParkingStay save(ParkingStay stay) {
        return toDomain(repository.saveAndFlush(toEntity(stay)));
    }

    @Override
    public Optional<ParkingStay> findActiveByPlate(LicensePlate plate) {
        return repository.findByLicensePlateAndStatus(plate.value(), StayStatus.ACTIVE)
                .map(this::toDomain);
    }

    @Override
    public Optional<ParkingStay> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ParkingStay> findAll(LicensePlate plate, StayStatus status) {
        List<ParkingStayEntity> entities;
        if (plate != null && status != null) {
            entities = repository.findAllByLicensePlateAndStatusOrderByEnteredAtDesc(plate.value(), status);
        } else if (plate != null) {
            entities = repository.findAllByLicensePlateOrderByEnteredAtDesc(plate.value());
        } else if (status != null) {
            entities = repository.findAllByStatusOrderByEnteredAtDesc(status);
        } else {
            entities = repository.findAllByOrderByEnteredAtDesc();
        }
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public List<ParkingStay> findAllActive() {
        return repository.findAllByStatusOrderByEnteredAtAsc(StayStatus.ACTIVE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countActive() {
        return repository.countByStatus(StayStatus.ACTIVE);
    }

    private ParkingStayEntity toEntity(ParkingStay stay) {
        return new ParkingStayEntity(
                stay.id(),
                stay.licensePlate().value(),
                stay.enteredAt(),
                stay.exitedAt(),
                stay.status(),
                stay.amountCharged());
    }

    private ParkingStay toDomain(ParkingStayEntity entity) {
        return ParkingStay.restore(
                entity.getId(),
                LicensePlate.of(entity.getLicensePlate()),
                entity.getEnteredAt(),
                entity.getExitedAt(),
                entity.getStatus(),
                entity.getAmountCharged());
    }
}
