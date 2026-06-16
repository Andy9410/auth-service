package com.academy.authservice.analytics.dto;

public record AnalyticsOverviewResponse(
        long registeredUsers,
        long activeUsers,
        long requests,
        long errors,
        long logins,
        long chatMessages,
        long documentsUploaded,
        long slowRequests
) {}
