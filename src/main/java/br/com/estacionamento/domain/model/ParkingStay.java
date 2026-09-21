package br.com.estacionamento.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public final class ParkingStay {

    private final UUID id;
    private final LicensePlate licensePlate;
    private final Instant enteredAt;
    private Instant exitedAt;
    private StayStatus status;
    private BigDecimal amountCharged;

    private ParkingStay(
            UUID id,
            LicensePlate licensePlate,
            Instant enteredAt,
            Instant exitedAt,
            StayStatus status,
            BigDecimal amountCharged) {
        this.id = Objects.requireNonNull(id);
        this.licensePlate = Objects.requireNonNull(licensePlate);
        this.enteredAt = Objects.requireNonNull(enteredAt);
        this.exitedAt = exitedAt;
        this.status = Objects.requireNonNull(status);
        this.amountCharged = amountCharged;
        validateState();
    }

    public static ParkingStay start(UUID id, LicensePlate licensePlate, Instant enteredAt) {
        return new ParkingStay(id, licensePlate, enteredAt, null, StayStatus.ACTIVE, null);
    }

    public static ParkingStay restore(
            UUID id,
            LicensePlate licensePlate,
            Instant enteredAt,
            Instant exitedAt,
            StayStatus status,
            BigDecimal amountCharged) {
        return new ParkingStay(id, licensePlate, enteredAt, exitedAt, status, amountCharged);
    }

    public void finish(Instant exitTime, BigDecimal chargedAmount) {
        if (status == StayStatus.FINISHED) {
            throw new IllegalStateException("A estadia já foi finalizada");
        }
        if (exitTime.isBefore(enteredAt)) {
            throw new IllegalArgumentException("A saída não pode ocorrer antes da entrada");
        }
        if (chargedAmount == null || chargedAmount.signum() < 0) {
            throw new IllegalArgumentException("O valor cobrado deve ser válido");
        }
        this.exitedAt = exitTime;
        this.amountCharged = chargedAmount;
        this.status = StayStatus.FINISHED;
    }

    private void validateState() {
        boolean activeIsValid = status == StayStatus.ACTIVE && exitedAt == null && amountCharged == null;
        boolean finishedIsValid = status == StayStatus.FINISHED && exitedAt != null && amountCharged != null;
        if (!activeIsValid && !finishedIsValid) {
            throw new IllegalArgumentException("Estado da estadia inconsistente");
        }
        if (exitedAt != null && exitedAt.isBefore(enteredAt)) {
            throw new IllegalArgumentException("A saída não pode ocorrer antes da entrada");
        }
    }

    public long durationMinutes() {
        if (exitedAt == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(enteredAt, exitedAt);
    }

    public UUID id() {
        return id;
    }

    public LicensePlate licensePlate() {
        return licensePlate;
    }

    public Instant enteredAt() {
        return enteredAt;
    }

    public Instant exitedAt() {
        return exitedAt;
    }

    public StayStatus status() {
        return status;
    }

    public BigDecimal amountCharged() {
        return amountCharged;
    }
}
