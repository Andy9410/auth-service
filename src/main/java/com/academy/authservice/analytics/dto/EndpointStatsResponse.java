package com.academy.authservice.analytics.dto;

public record EndpointStatsResponse(
        String serviceName,
        String method,
        String endpoint,
        long value
) {}
