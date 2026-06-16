CREATE TABLE IF NOT EXISTS analytics_events (
    id           BIGSERIAL PRIMARY KEY,
    event_type   VARCHAR(40)  NOT NULL,
    user_email   VARCHAR(150),
    service_name  VARCHAR(80)  NOT NULL,
    feature      VARCHAR(40)  NOT NULL,
    method       VARCHAR(10),
    endpoint     VARCHAR(255),
    status_code  INT,
    duration_ms  BIGINT,
    is_slow      BOOLEAN      NOT NULL DEFAULT FALSE,
    error_type   VARCHAR(120),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_events_created_at
    ON analytics_events(created_at);

CREATE INDEX IF NOT EXISTS idx_analytics_events_event_type
    ON analytics_events(event_type);

CREATE INDEX IF NOT EXISTS idx_analytics_events_user_email
    ON analytics_events(user_email);

CREATE INDEX IF NOT EXISTS idx_analytics_events_feature
    ON analytics_events(feature);

CREATE INDEX IF NOT EXISTS idx_analytics_events_endpoint
    ON analytics_events(endpoint);
