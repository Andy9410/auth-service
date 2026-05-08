package com.academy.authservice.dto;

import com.academy.authservice.model.User;

import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        Instant createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getCreatedAt()
        );
    }
}
