package com.academy.authservice.service;

import com.academy.authservice.dto.AdminUserFilters;
import com.academy.authservice.dto.AdminUserLookupResponse;
import com.academy.authservice.dto.AdminUserPageResponse;
import com.academy.authservice.repository.UserRepository;
import com.academy.authservice.repository.AdminUserRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final UserRepository userRepository;

    public AdminUserService(AdminUserRepository adminUserRepository, UserRepository userRepository) {
        this.adminUserRepository = adminUserRepository;
        this.userRepository = userRepository;
    }

    public AdminUserPageResponse getUsers(AdminUserFilters filters, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return adminUserRepository.findPage(filters, safePage, safeSize);
    }

    public List<AdminUserLookupResponse> lookupByEmails(Collection<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }
        return userRepository.findByEmailIn(emails)
                .stream()
                .map(user -> new AdminUserLookupResponse(
                        user.getEmail(),
                        user.getName(),
                        user.getRole().getName()
                ))
                .toList();
    }
}
