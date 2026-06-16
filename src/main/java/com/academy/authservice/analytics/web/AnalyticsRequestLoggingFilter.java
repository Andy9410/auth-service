package com.academy.authservice.analytics.web;

import com.academy.authservice.analytics.model.AnalyticsFeature;
import com.academy.authservice.analytics.service.AnalyticsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
public class AnalyticsRequestLoggingFilter extends OncePerRequestFilter {

    public static final String ERROR_RECORDED_ATTRIBUTE = "analytics.errorRecorded";

    private final AnalyticsService analyticsService;

    public AnalyticsRequestLoggingFilter(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var uri = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || uri.startsWith("/actuator/")
                || uri.equals("/auth/health")
                || uri.equals("/auth/info")
                || uri.startsWith("/admin/metrics/")
                || uri.startsWith("/internal/analytics/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startedAt;
            String userEmail = currentUserEmail();
            var feature = inferFeature(request.getRequestURI());

            analyticsService.recordRequest(
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    userEmail,
                    feature
            );

            if (response.getStatus() >= 400 && !Boolean.TRUE.equals(request.getAttribute(ERROR_RECORDED_ATTRIBUTE))) {
                analyticsService.recordError(
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        userEmail,
                        feature,
                        "HTTP_" + response.getStatus()
                );
            }
        }
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }
        String name = authentication.getName();
        if ("anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name == null || name.isBlank() ? null : name;
    }

    private AnalyticsFeature inferFeature(String path) {
        var normalized = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/auth")) {
            return AnalyticsFeature.AUTH;
        }
        return AnalyticsFeature.OTHER;
    }
}
