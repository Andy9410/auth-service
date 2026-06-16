package com.academy.authservice.analytics.web;

import com.academy.authservice.analytics.dto.*;
import com.academy.authservice.analytics.service.AnalyticsReportService;
import com.academy.authservice.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsReportService analyticsReportService;

    public AnalyticsController(AnalyticsService analyticsService,
                               AnalyticsReportService analyticsReportService) {
        this.analyticsService = analyticsService;
        this.analyticsReportService = analyticsReportService;
    }

    @PostMapping("/internal/analytics/events")
    public ResponseEntity<Void> ingest(@Valid @RequestBody AnalyticsEventRequest request) {
        analyticsService.recordEvent(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/admin/metrics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsOverviewResponse> overview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String feature
    ) {
        return ResponseEntity.ok(analyticsReportService.overview(from, to, feature));
    }

    @GetMapping("/admin/metrics/timeseries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnalyticsSeriesPoint>> timeSeries(
            @RequestParam AnalyticsMetricType metric,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String feature
    ) {
        return ResponseEntity.ok(analyticsReportService.timeSeries(metric, from, to, feature));
    }

    @GetMapping("/admin/metrics/endpoints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EndpointStatsResponse>> topEndpoints(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String feature,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsReportService.topEndpoints(from, to, feature, limit));
    }

    @GetMapping("/admin/metrics/users/top")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopUserResponse>> topUsers(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String feature,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsReportService.topUsersByRequests(from, to, feature, limit));
    }
}
