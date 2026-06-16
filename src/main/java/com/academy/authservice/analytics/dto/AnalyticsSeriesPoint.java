package com.academy.authservice.analytics.dto;

import java.time.LocalDate;

public record AnalyticsSeriesPoint(
        LocalDate day,
        long value
) {}
