package br.com.estacionamento.application.port;

import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingStayRepository {

    ParkingStay save(ParkingStay stay);

    Optional<ParkingStay> findActiveByPlate(LicensePlate plate);

    Optional<ParkingStay> findById(UUID id);

    List<ParkingStay> findAll(LicensePlate plate, StayStatus status);

    List<ParkingStay> findAllActive();

    long countActive();
}
