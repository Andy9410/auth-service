CREATE INDEX IF NOT EXISTS idx_analytics_events_created_at_event_type
    ON analytics_events(created_at, event_type);

CREATE INDEX IF NOT EXISTS idx_analytics_events_feature_created_at_event_type
    ON analytics_events(feature, created_at, event_type);

CREATE INDEX IF NOT EXISTS idx_analytics_events_user_email_created_at
    ON analytics_events(user_email, created_at);

CREATE INDEX IF NOT EXISTS idx_analytics_events_event_type_user_email_created_at
    ON analytics_events(event_type, user_email, created_at);
