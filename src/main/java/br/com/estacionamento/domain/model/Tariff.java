package br.com.estacionamento.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Tariff(BigDecimal initialFee, BigDecimal hourlyFee, long gracePeriodMinutes) {

    public Tariff {
        Objects.requireNonNull(initialFee, "O valor inicial é obrigatório");
        Objects.requireNonNull(hourlyFee, "O valor por hora é obrigatório");
        if (initialFee.signum() < 0 || hourlyFee.signum() < 0) {
            throw new IllegalArgumentException("Os valores da tarifa não podem ser negativos");
        }
        if (gracePeriodMinutes < 0) {
            throw new IllegalArgumentException("A tolerância não pode ser negativa");
        }
    }
}
