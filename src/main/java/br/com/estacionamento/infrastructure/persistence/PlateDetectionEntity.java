package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.domain.model.DetectionDirection;
import br.com.estacionamento.domain.model.DetectionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plate_detections")
public class PlateDetectionEntity {
    @Id private UUID id;
    @Column(name = "raw_plate", nullable = false, length = 32) private String rawPlate;
    @Column(nullable = false) private double confidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 8) private DetectionDirection direction;
    @Column(name = "detected_at", nullable = false) private Instant detectedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private DetectionStatus status;
    @Column(length = 500) private String reason;
    @Column(name = "resolved_at") private Instant resolvedAt;

    protected PlateDetectionEntity() { }

    public PlateDetectionEntity(UUID id, String rawPlate, double confidence, DetectionDirection direction,
                                Instant detectedAt, DetectionStatus status, String reason, Instant resolvedAt) {
        this.id = id; this.rawPlate = rawPlate; this.confidence = confidence; this.direction = direction;
        this.detectedAt = detectedAt; this.status = status; this.reason = reason; this.resolvedAt = resolvedAt;
    }
    public UUID getId() { return id; }
    public String getRawPlate() { return rawPlate; }
    public double getConfidence() { return confidence; }
    public DetectionDirection getDirection() { return direction; }
    public Instant getDetectedAt() { return detectedAt; }
    public DetectionStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getResolvedAt() { return resolvedAt; }
}
