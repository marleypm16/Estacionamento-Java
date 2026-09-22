package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.application.port.PlateDetectionRepository;
import br.com.estacionamento.domain.model.DetectionStatus;
import br.com.estacionamento.domain.model.PlateDetection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPlateDetectionRepository implements PlateDetectionRepository {
    private final SpringDataPlateDetectionRepository repository;
    public JpaPlateDetectionRepository(SpringDataPlateDetectionRepository repository) { this.repository = repository; }
    @Override public PlateDetection save(PlateDetection detection) { return toDomain(repository.save(toEntity(detection))); }
    @Override public Optional<PlateDetection> findById(UUID id) { return repository.findById(id).map(this::toDomain); }
    @Override public List<PlateDetection> findAllByStatus(DetectionStatus status) {
        return repository.findAllByStatusOrderByDetectedAtAsc(status).stream().map(this::toDomain).toList();
    }
    private PlateDetectionEntity toEntity(PlateDetection d) {
        return new PlateDetectionEntity(d.id(), d.rawPlate(), d.confidence(), d.direction(), d.detectedAt(), d.status(), d.reason(), d.resolvedAt());
    }
    private PlateDetection toDomain(PlateDetectionEntity d) {
        return PlateDetection.restore(d.getId(), d.getRawPlate(), d.getConfidence(), d.getDirection(), d.getDetectedAt(), d.getStatus(), d.getReason(), d.getResolvedAt());
    }
}
