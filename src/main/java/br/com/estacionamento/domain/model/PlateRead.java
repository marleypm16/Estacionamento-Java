package br.com.estacionamento.domain.model;

import java.time.Instant;
import java.util.Objects;

/** Provider-neutral OCR payload; no camera SDK type leaks into the application. */
public record PlateRead(String rawPlate, double confidence, DetectionDirection direction, Instant detectedAt) {
    public PlateRead {
        Objects.requireNonNull(rawPlate);
        Objects.requireNonNull(direction);
        Objects.requireNonNull(detectedAt);
        if (confidence < 0 || confidence > 1) throw new IllegalArgumentException("A confiança deve estar entre 0 e 1");
    }
}
