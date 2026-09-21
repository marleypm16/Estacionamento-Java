package br.com.estacionamento.interfaces.rest.dto;

import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.StayStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ParkingStayResponse(
        UUID id,
        String plate,
        Instant enteredAt,
        Instant exitedAt,
        StayStatus status,
        BigDecimal amountCharged,
        Long durationMinutes) {

    public static ParkingStayResponse from(ParkingStay stay) {
        Long duration = stay.exitedAt() == null ? null : stay.durationMinutes();
        return new ParkingStayResponse(
                stay.id(),
                stay.licensePlate().value(),
                stay.enteredAt(),
                stay.exitedAt(),
                stay.status(),
                stay.amountCharged(),
                duration);
    }
}
