package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.domain.model.StayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataParkingStayRepository extends JpaRepository<ParkingStayEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingStayEntity> findByLicensePlateAndStatus(String licensePlate, StayStatus status);

    List<ParkingStayEntity> findAllByStatusOrderByEnteredAtAsc(StayStatus status);

    List<ParkingStayEntity> findAllByOrderByEnteredAtDesc();

    List<ParkingStayEntity> findAllByLicensePlateOrderByEnteredAtDesc(String licensePlate);

    List<ParkingStayEntity> findAllByStatusOrderByEnteredAtDesc(StayStatus status);

    List<ParkingStayEntity> findAllByLicensePlateAndStatusOrderByEnteredAtDesc(
            String licensePlate, StayStatus status);

    long countByStatus(StayStatus status);
}
