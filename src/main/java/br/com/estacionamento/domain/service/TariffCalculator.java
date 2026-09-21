package br.com.estacionamento.domain.service;

import br.com.estacionamento.domain.model.Tariff;

import java.math.BigDecimal;
import java.time.Instant;

public interface TariffCalculator {

    BigDecimal calculate(Instant enteredAt, Instant exitedAt, Tariff tariff);
}
