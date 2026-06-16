package com.academy.authservice.analytics.dto;

public record TopUserResponse(
        String email,
        String name,
        long value
) {}
