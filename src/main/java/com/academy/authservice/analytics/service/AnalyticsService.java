package com.academy.authservice.analytics.service;

import com.academy.authservice.analytics.dto.AnalyticsEventRequest;
import com.academy.authservice.analytics.model.AnalyticsEvent;
import com.academy.authservice.analytics.model.AnalyticsEventType;
import com.academy.authservice.analytics.model.AnalyticsFeature;
import com.academy.authservice.analytics.repository.AnalyticsEventRepository;
import com.academy.authservice.analytics.util.AnalyticsPathNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsEventRepository repository;
    private final String serviceName;
    private final long slowThresholdMs;
    private final boolean requestTrackingEnabled;

    public AnalyticsService(AnalyticsEventRepository repository,
                            @Value("${spring.application.name:auth-service}") String serviceName,
                            @Value("${analytics.slow-request-threshold-ms:1000}") long slowThresholdMs,
                            @Value("${analytics.request-tracking-enabled:true}") boolean requestTrackingEnabled) {
        this.repository = repository;
        this.serviceName = serviceName;
        this.slowThresholdMs = slowThresholdMs;
        this.requestTrackingEnabled = requestTrackingEnabled;
    }

    public void recordEvent(AnalyticsEventRequest request) {
        persist(buildEvent(request));
    }

    public void recordRequest(String method,
                              String endpoint,
                              Integer statusCode,
                              Long durationMs,
                              String userEmail,
                              AnalyticsFeature feature) {
        if (!requestTrackingEnabled) {
            return;
        }
        var event = new AnalyticsEvent();
        event.setEventType(AnalyticsEventType.REQUEST);
        event.setUserEmail(userEmail);
        event.setServiceName(serviceName);
        event.setFeature(feature);
        event.setMethod(method);
        event.setEndpoint(AnalyticsPathNormalizer.normalize(endpoint));
        event.setStatusCode(statusCode);
        event.setDurationMs(durationMs);
        event.setSlow(durationMs != null && durationMs >= slowThresholdMs);
        persist(event);
    }

    public void recordLogin(String userEmail) {
        var event = new AnalyticsEvent();
        event.setEventType(AnalyticsEventType.LOGIN);
        event.setUserEmail(userEmail);
        event.setServiceName(serviceName);
        event.setFeature(AnalyticsFeature.AUTH);
        event.setEndpoint("/auth/login");
        persist(event);
    }

    public void recordError(String method,
                            String endpoint,
                            Integer statusCode,
                            String userEmail,
                            AnalyticsFeature feature,
                            String errorType) {
        if (!requestTrackingEnabled && statusCode != null && statusCode < 500) {
            return;
        }
        var event = new AnalyticsEvent();
        event.setEventType(AnalyticsEventType.ERROR);
        event.setUserEmail(userEmail);
        event.setServiceName(serviceName);
        event.setFeature(feature);
        event.setMethod(method);
        event.setEndpoint(AnalyticsPathNormalizer.normalize(endpoint));
        event.setStatusCode(statusCode);
        event.setErrorType(errorType);
        persist(event);
    }

    public void recordBusinessEvent(AnalyticsEventType eventType,
                                    String serviceName,
                                    String userEmail,
                                    AnalyticsFeature feature,
                                    String method,
                                    String endpoint,
                                    Integer statusCode,
                                    Long durationMs,
                                    String errorType) {
        var event = new AnalyticsEvent();
        event.setEventType(eventType);
        event.setUserEmail(userEmail);
        event.setServiceName(serviceName == null || serviceName.isBlank() ? this.serviceName : serviceName);
        event.setFeature(feature == null ? AnalyticsFeature.OTHER : feature);
        event.setMethod(method);
        event.setEndpoint(endpoint == null ? null : AnalyticsPathNormalizer.normalize(endpoint));
        event.setStatusCode(statusCode);
        event.setDurationMs(durationMs);
        event.setSlow(durationMs != null && durationMs >= slowThresholdMs);
        event.setErrorType(errorType);
        persist(event);
    }

    public long getSlowThresholdMs() {
        return slowThresholdMs;
    }

    private AnalyticsEvent buildEvent(AnalyticsEventRequest request) {
        var event = new AnalyticsEvent();
        event.setEventType(request.eventType());
        event.setUserEmail(blankToNull(request.userEmail()));
        event.setServiceName(blankToNull(request.serviceName()) == null ? serviceName : request.serviceName());
        event.setFeature(request.feature() == null ? AnalyticsFeature.OTHER : request.feature());
        event.setMethod(blankToNull(request.method()));
        event.setEndpoint(request.endpoint() == null ? null : AnalyticsPathNormalizer.normalize(request.endpoint()));
        event.setStatusCode(request.statusCode());
        event.setDurationMs(request.durationMs());
        event.setSlow(Boolean.TRUE.equals(request.isSlow())
                || (request.durationMs() != null && request.durationMs() >= slowThresholdMs));
        event.setErrorType(blankToNull(request.errorType()));
        return event;
    }

    private void persist(AnalyticsEvent event) {
        try {
            repository.save(event);
        } catch (Exception ex) {
            log.warn("Analytics event could not be persisted: {}", ex.getMessage());
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
