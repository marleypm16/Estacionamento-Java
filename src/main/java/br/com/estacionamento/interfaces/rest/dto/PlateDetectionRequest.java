package br.com.estacionamento.interfaces.rest.dto;

import br.com.estacionamento.domain.model.DetectionDirection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record PlateDetectionRequest(
        @NotBlank(message = "A leitura da placa é obrigatória") String plate,
        @DecimalMin(value = "0.0", message = "A confiança deve estar entre 0 e 1")
        @DecimalMax(value = "1.0", message = "A confiança deve estar entre 0 e 1") double confidence,
        @NotNull(message = "O sentido da detecção é obrigatório") DetectionDirection direction,
        Instant detectedAt) { }
