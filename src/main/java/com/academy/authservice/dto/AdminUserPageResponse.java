package com.academy.authservice.dto;

import java.util.List;

public record AdminUserPageResponse(
        List<AdminUserSummaryResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
