package com.academy.authservice.analytics.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "analytics_events")
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private AnalyticsEventType eventType;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "service_name", nullable = false, length = 80)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AnalyticsFeature feature;

    @Column(length = 10)
    private String method;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "is_slow", nullable = false)
    private boolean slow;

    @Column(name = "error_type", length = 120)
    private String errorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AnalyticsEvent() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AnalyticsEventType getEventType() {
        return eventType;
    }

    public void setEventType(AnalyticsEventType eventType) {
        this.eventType = eventType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public AnalyticsFeature getFeature() {
        return feature;
    }

    public void setFeature(AnalyticsFeature feature) {
        this.feature = feature;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isSlow() {
        return slow;
    }

    public void setSlow(boolean slow) {
        this.slow = slow;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
