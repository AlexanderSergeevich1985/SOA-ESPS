CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE devices (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    model_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- Raw ML metrics (пишет ingestion-консюмер из топика ml-metrics)
CREATE TABLE ml_metrics (
    ts              TIMESTAMPTZ NOT NULL DEFAULT now(),
    device_id       TEXT        NOT NULL,
    user_id         BIGINT      NOT NULL,
    metric_name     TEXT        NOT NULL,
    value           DOUBLE PRECISION NOT NULL,
    anomaly_score   DOUBLE PRECISION,
    predicted_state TEXT
);
SELECT create_hypertable('ml_metrics', 'ts');
CREATE INDEX idx_ml_metrics_device ON ml_metrics (device_id, ts DESC);

-- CONTINUOUS AGGREGATE: БД сама каждый час сворачивает сырьё в статистику.
-- Это и есть «сбор статистики за несколько часов» без единой строчки кода.
CREATE MATERIALIZED VIEW ml_metrics_hourly
WITH (timescaledb.continuous) AS
SELECT
    time_bucket(INTERVAL '1 hour', ts) AS bucket,
    device_id,
    user_id,
    metric_name,
    avg(value)     AS avg_value,
    min(value)     AS min_value,
    max(value)     AS max_value,
    stddev(value)  AS stddev_value,
    max(anomaly_score) AS max_anomaly
FROM ml_metrics
GROUP BY bucket, device_id, user_id, metric_name;

SELECT add_continuous_aggregate_policy('ml_metrics_hourly',
       start_offset    => INTERVAL '3 hours',
       end_offset      => INTERVAL '1 hour',
       schedule_interval => INTERVAL '30 minutes');