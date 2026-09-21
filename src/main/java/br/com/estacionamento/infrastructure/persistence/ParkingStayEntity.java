package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.domain.model.StayStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parking_stays")
public class ParkingStayEntity {

    @Id
    private UUID id;

    @Column(name = "license_plate", nullable = false, length = 7)
    private String licensePlate;

    @Column(name = "entered_at", nullable = false)
    private Instant enteredAt;

    @Column(name = "exited_at")
    private Instant exitedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StayStatus status;

    @Column(name = "amount_charged", precision = 12, scale = 2)
    private BigDecimal amountCharged;

    @Version
    @Column(nullable = false)
    private long version;

    protected ParkingStayEntity() {
    }

    public ParkingStayEntity(
            UUID id,
            String licensePlate,
            Instant enteredAt,
            Instant exitedAt,
            StayStatus status,
            BigDecimal amountCharged) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.enteredAt = enteredAt;
        this.exitedAt = exitedAt;
        this.status = status;
        this.amountCharged = amountCharged;
    }

    public UUID getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public Instant getExitedAt() {
        return exitedAt;
    }

    public StayStatus getStatus() {
        return status;
    }

    public BigDecimal getAmountCharged() {
        return amountCharged;
    }
}
