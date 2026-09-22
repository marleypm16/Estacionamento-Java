package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.domain.model.DetectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface SpringDataPlateDetectionRepository extends JpaRepository<PlateDetectionEntity, UUID> {
    List<PlateDetectionEntity> findAllByStatusOrderByDetectedAtAsc(DetectionStatus status);
}
