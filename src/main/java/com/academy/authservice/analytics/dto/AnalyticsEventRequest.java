package com.academy.authservice.analytics.dto;

import com.academy.authservice.analytics.model.AnalyticsEventType;
import com.academy.authservice.analytics.model.AnalyticsFeature;
import jakarta.validation.constraints.NotNull;

public record AnalyticsEventRequest(
        @NotNull AnalyticsEventType eventType,
        String userEmail,
        @NotNull String serviceName,
        @NotNull AnalyticsFeature feature,
        String method,
        String endpoint,
        Integer statusCode,
        Long durationMs,
        Boolean isSlow,
        String errorType
) {}
