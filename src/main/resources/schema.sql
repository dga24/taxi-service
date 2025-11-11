DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS ride_view;

CREATE TABLE IF NOT EXISTS events
(
    id          IDENTITY PRIMARY KEY,
    ride_id     VARCHAR(36)  NOT NULL,
    seq         INT          NOT NULL,
    type        VARCHAR(100) NOT NULL,
    payload     VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP    NOT NULL,
    UNIQUE (ride_id, seq)
);

CREATE TABLE IF NOT EXISTS ride_view
(
    ride_id     VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    driver_id   VARCHAR(36),
    origin      VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);
