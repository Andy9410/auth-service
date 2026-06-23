package com.academy.authservice.controller;

import com.academy.authservice.dto.AdminUserFilters;
import com.academy.authservice.dto.AdminUserLookupResponse;
import com.academy.authservice.dto.AdminUserPageResponse;
import com.academy.authservice.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserPageResponse> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(new AdminUserFilters(email, name), page, size));
    }

    @GetMapping("/admin/users/lookup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserLookupResponse>> lookup(
            @RequestParam List<String> emails
    ) {
        return ResponseEntity.ok(adminUserService.lookupByEmails(emails));
    }
}
