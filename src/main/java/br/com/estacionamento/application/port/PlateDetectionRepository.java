package br.com.estacionamento.application.port;

import br.com.estacionamento.domain.model.DetectionStatus;
import br.com.estacionamento.domain.model.PlateDetection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlateDetectionRepository {
    PlateDetection save(PlateDetection detection);
    Optional<PlateDetection> findById(UUID id);
    List<PlateDetection> findAllByStatus(DetectionStatus status);
}
