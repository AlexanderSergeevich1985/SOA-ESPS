-- Layout table using a Composite Primary Key for strict constraint enforcement
CREATE TABLE IF NOT EXISTS notification_templates (
    notification_type VARCHAR(100) NOT NULL,            -- Part 1 of Composite Key (e.g., 'PAYMENT_SUCCESS')
    channel_type VARCHAR(50) NOT NULL,                 -- Part 2 of Composite Key (e.g., 'SMS')
    is_external_storage BOOLEAN DEFAULT FALSE NOT NULL, -- FALSE for Postgres text, TRUE for MinIO HTML
    inline_text_template TEXT,                          -- Text layout for SMS/Telegram/Push
    minio_object_key VARCHAR(255),                      -- Path in MinIO bucket if is_external_storage is TRUE
    PRIMARY KEY (notification_type, channel_type)       -- Enforces uniqueness on DB layer level
);