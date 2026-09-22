package br.com.estacionamento.interfaces.rest.dto;

import br.com.estacionamento.domain.model.DetectionDirection;
import br.com.estacionamento.domain.model.DetectionStatus;
import br.com.estacionamento.domain.model.PlateDetection;
import java.time.Instant;
import java.util.UUID;

public record PlateDetectionResponse(UUID id, String rawPlate, double confidence, DetectionDirection direction,
                                     Instant detectedAt, DetectionStatus status, String reason, Instant resolvedAt) {
    public static PlateDetectionResponse from(PlateDetection detection) {
        return new PlateDetectionResponse(detection.id(), detection.rawPlate(), detection.confidence(), detection.direction(), detection.detectedAt(), detection.status(), detection.reason(), detection.resolvedAt());
    }
}
