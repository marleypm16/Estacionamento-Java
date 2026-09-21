package br.com.estacionamento.domain.service;

import br.com.estacionamento.domain.model.Tariff;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public final class DefaultTariffCalculator implements TariffCalculator {

    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3_600);

    @Override
    public BigDecimal calculate(Instant enteredAt, Instant exitedAt, Tariff tariff) {
        if (exitedAt.isBefore(enteredAt)) {
            throw new IllegalArgumentException("A saída não pode ocorrer antes da entrada");
        }

        Duration duration = Duration.between(enteredAt, exitedAt);
        if (duration.compareTo(Duration.ofMinutes(tariff.gracePeriodMinutes())) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        long chargedHours = BigDecimal.valueOf(Math.max(duration.toSeconds(), 1))
                .divide(SECONDS_PER_HOUR, 0, RoundingMode.CEILING)
                .longValueExact();
        long additionalHours = Math.max(chargedHours - 1, 0);

        return tariff.initialFee()
                .add(tariff.hourlyFee().multiply(BigDecimal.valueOf(additionalHours)))
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
