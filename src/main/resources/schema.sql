CREATE TABLE IF NOT EXISTS events
(
    id IDENTITY PRIMARY KEY,
    ride_id     VARCHAR(36)  NOT NULL,
    seq         INT          NOT NULL,
    type        VARCHAR(100) NOT NULL,
    payload     VARCHAR(255) NOT NULL,
    occurred_at DATETIME     NOT NULL,
    UNIQUE (ride_id, seq)
);
