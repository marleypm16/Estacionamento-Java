package br.com.estacionamento.domain.service;

import br.com.estacionamento.domain.model.Tariff;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTariffCalculatorTest {

    private final DefaultTariffCalculator calculator = new DefaultTariffCalculator();
    private final Tariff tariff = new Tariff(new BigDecimal("8.00"), new BigDecimal("4.00"), 0);
    private final Instant entry = Instant.parse("2026-09-21T10:00:00Z");

    @Test
    void shouldChargeInitialFeeForUpToOneHour() {
        assertThat(calculator.calculate(entry, entry.plusSeconds(3_600), tariff))
                .isEqualByComparingTo("8.00");
    }

    @Test
    void shouldChargeEveryAdditionalStartedHour() {
        assertThat(calculator.calculate(entry, entry.plusSeconds(3_601), tariff))
                .isEqualByComparingTo("12.00");
        assertThat(calculator.calculate(entry, entry.plusSeconds(7_201), tariff))
                .isEqualByComparingTo("16.00");
    }

    @Test
    void shouldNotChargeInsideGracePeriod() {
        Tariff withGrace = new Tariff(new BigDecimal("8.00"), new BigDecimal("4.00"), 10);
        assertThat(calculator.calculate(entry, entry.plusSeconds(600), withGrace))
                .isEqualByComparingTo("0.00");
    }
}
