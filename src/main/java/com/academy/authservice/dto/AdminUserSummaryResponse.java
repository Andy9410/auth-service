package com.academy.authservice.dto;

import java.time.Instant;

public record AdminUserSummaryResponse(
        Long id,
        String name,
        String email,
        String role,
        Instant createdAt,
        Instant lastLoginAt,
        boolean active
) {}
