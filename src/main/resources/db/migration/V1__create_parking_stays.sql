CREATE TABLE parking_stays (
    id UUID PRIMARY KEY,
    license_plate VARCHAR(7) NOT NULL,
    entered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    exited_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(16) NOT NULL,
    amount_charged NUMERIC(12, 2),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_parking_stays_status CHECK (status IN ('ACTIVE', 'FINISHED')),
    CONSTRAINT ck_parking_stays_state CHECK (
        (status = 'ACTIVE' AND exited_at IS NULL AND amount_charged IS NULL)
        OR
        (status = 'FINISHED' AND exited_at IS NOT NULL AND amount_charged IS NOT NULL)
    ),
    CONSTRAINT ck_parking_stays_amount CHECK (amount_charged IS NULL OR amount_charged >= 0),
    CONSTRAINT ck_parking_stays_dates CHECK (exited_at IS NULL OR exited_at >= entered_at)
);

CREATE INDEX idx_parking_stays_license_plate ON parking_stays (license_plate);
CREATE INDEX idx_parking_stays_entered_at ON parking_stays (entered_at DESC);
CREATE UNIQUE INDEX uq_parking_stays_active_plate
    ON parking_stays (license_plate)
    WHERE status = 'ACTIVE';
