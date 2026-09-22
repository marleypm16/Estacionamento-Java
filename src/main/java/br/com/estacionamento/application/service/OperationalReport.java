package br.com.estacionamento.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OperationalReport(
        LocalDate from,
        LocalDate to,
        long entries,
        long exits,
        BigDecimal revenue,
        long averageStayMinutes) { }
