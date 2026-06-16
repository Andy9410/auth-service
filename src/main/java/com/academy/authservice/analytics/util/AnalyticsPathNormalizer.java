package com.academy.authservice.analytics.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AnalyticsPathNormalizer {

    private static final Pattern UUID_SEGMENT = Pattern.compile("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern NUMERIC_SEGMENT = Pattern.compile("^\\d+$");
    private static final Pattern LONG_HEX_SEGMENT = Pattern.compile("(?i)^[0-9a-f]{12,}$");

    private AnalyticsPathNormalizer() {}

    public static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        var sanitized = path.split("\\?", 2)[0];
        var segments = sanitized.split("/");
        var normalized = new StringBuilder();

        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }

            normalized.append('/');
            normalized.append(normalizeSegment(segment));
        }

        return normalized.length() == 0 ? "/" : normalized.toString();
    }

    private static String normalizeSegment(String segment) {
        if (NUMERIC_SEGMENT.matcher(segment).matches() || UUID_SEGMENT.matcher(segment).matches()) {
            return "{id}";
        }

        if (LONG_HEX_SEGMENT.matcher(segment).matches()) {
            return "{id}";
        }

        return segment.toLowerCase(Locale.ROOT);
    }
}
