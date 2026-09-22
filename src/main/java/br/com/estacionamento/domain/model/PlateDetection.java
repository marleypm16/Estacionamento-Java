package br.com.estacionamento.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A camera/OCR observation. It is intentionally separate from a parking stay. */
public final class PlateDetection {
    private final UUID id;
    private final String rawPlate;
    private final Double confidence;
    private final DetectionDirection direction;
    private final Instant detectedAt;
    private DetectionStatus status;
    private String reason;
    private Instant resolvedAt;

    private PlateDetection(UUID id, String rawPlate, Double confidence, DetectionDirection direction,
                           Instant detectedAt, DetectionStatus status, String reason, Instant resolvedAt) {
        this.id = Objects.requireNonNull(id);
        this.rawPlate = Objects.requireNonNull(rawPlate);
        this.confidence = Objects.requireNonNull(confidence);
        this.direction = Objects.requireNonNull(direction);
        this.detectedAt = Objects.requireNonNull(detectedAt);
        this.status = Objects.requireNonNull(status);
        this.reason = reason;
        this.resolvedAt = resolvedAt;
        if (confidence < 0 || confidence > 1) throw new IllegalArgumentException("A confiança deve estar entre 0 e 1");
    }

    public static PlateDetection receive(UUID id, String rawPlate, double confidence, DetectionDirection direction, Instant detectedAt) {
        return new PlateDetection(id, rawPlate.trim(), confidence, direction, detectedAt, DetectionStatus.PENDING, null, null);
    }

    public static PlateDetection restore(UUID id, String rawPlate, double confidence, DetectionDirection direction,
                                         Instant detectedAt, DetectionStatus status, String reason, Instant resolvedAt) {
        return new PlateDetection(id, rawPlate, confidence, direction, detectedAt, status, reason, resolvedAt);
    }

    public void process(Instant resolvedAt) { resolve(DetectionStatus.PROCESSED, null, resolvedAt); }
    public void keepPending(String reason) { this.reason = Objects.requireNonNull(reason); }
    public void dismiss(String reason, Instant resolvedAt) { resolve(DetectionStatus.DISMISSED, reason, resolvedAt); }

    private void resolve(DetectionStatus nextStatus, String nextReason, Instant nextResolvedAt) {
        if (status != DetectionStatus.PENDING) throw new IllegalStateException("A detecção já foi tratada");
        status = nextStatus;
        reason = nextReason;
        resolvedAt = Objects.requireNonNull(nextResolvedAt);
    }

    public UUID id() { return id; }
    public String rawPlate() { return rawPlate; }
    public Double confidence() { return confidence; }
    public DetectionDirection direction() { return direction; }
    public Instant detectedAt() { return detectedAt; }
    public DetectionStatus status() { return status; }
    public String reason() { return reason; }
    public Instant resolvedAt() { return resolvedAt; }
}
