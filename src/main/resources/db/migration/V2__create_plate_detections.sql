CREATE TABLE plate_detections (
    id UUID PRIMARY KEY,
    raw_plate VARCHAR(32) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    direction VARCHAR(8) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(16) NOT NULL,
    reason VARCHAR(500),
    resolved_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_plate_detections_confidence CHECK (confidence >= 0 AND confidence <= 1),
    CONSTRAINT ck_plate_detections_direction CHECK (direction IN ('ENTRY', 'EXIT')),
    CONSTRAINT ck_plate_detections_status CHECK (status IN ('PENDING', 'PROCESSED', 'DISMISSED'))
);

CREATE INDEX idx_plate_detections_pending ON plate_detections (status, detected_at);
