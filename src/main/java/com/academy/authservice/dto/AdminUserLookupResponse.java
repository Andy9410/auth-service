package com.academy.authservice.dto;

public record AdminUserLookupResponse(
        String email,
        String name,
        String role
) {}
