package com.academy.authservice.config;

import com.academy.authservice.analytics.model.AnalyticsFeature;
import com.academy.authservice.analytics.service.AnalyticsService;
import com.academy.authservice.analytics.web.AnalyticsRequestLoggingFilter;
import com.academy.authservice.dto.ErrorResponse;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final AnalyticsService analyticsService;

    public GlobalExceptionHandler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        recordError(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                              HttpServletRequest request) {
        recordError(request, HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex,
                                                        HttpServletRequest request) {
        recordError(request, HttpStatus.FORBIDDEN, "DISABLED_ACCOUNT");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Account is disabled"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        recordError(request, HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT");
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                            HttpServletRequest request) {
        recordError(request, HttpStatus.CONFLICT, "ILLEGAL_STATE");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                      HttpServletRequest request) {
        recordError(request, HttpStatus.INTERNAL_SERVER_ERROR, "UNHANDLED_EXCEPTION");
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        Sentry.captureException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }

    private void recordError(HttpServletRequest request, HttpStatus status, String errorType) {
        request.setAttribute(AnalyticsRequestLoggingFilter.ERROR_RECORDED_ATTRIBUTE, true);
        analyticsService.recordError(
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                null,
                inferFeature(request.getRequestURI()),
                errorType
        );
    }

    private AnalyticsFeature inferFeature(String path) {
        if (path != null && path.startsWith("/auth")) {
            return AnalyticsFeature.AUTH;
        }
        return AnalyticsFeature.OTHER;
    }
}
