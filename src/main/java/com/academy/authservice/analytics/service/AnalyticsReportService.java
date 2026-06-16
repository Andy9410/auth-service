package com.academy.authservice.analytics.service;

import com.academy.authservice.analytics.dto.*;
import com.academy.authservice.analytics.model.AnalyticsEventType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsReportService {

    private static final String DEFAULT_FEATURE_FILTER = "ALL";

    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsReportService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse overview(LocalDate from, LocalDate to, String feature) {
        var range = normalizeRange(from, to);
        var params = params(range.fromInstant(), range.toExclusive(), feature);

        long registeredUsers = queryLong("""
                SELECT COUNT(*)
                FROM users u
                WHERE u.created_at >= :from
                  AND u.created_at < :toExclusive
                """, params);

        long activeUsers = queryLong("""
                SELECT COUNT(DISTINCT e.user_email)
                FROM analytics_events e
                WHERE e.created_at >= :from
                  AND e.created_at < :toExclusive
                  AND e.user_email IS NOT NULL
                  AND e.event_type IN ('REQUEST', 'LOGIN', 'CHAT_MESSAGE', 'DOCUMENT_UPLOAD', 'ERROR')
                """ + featureFilterClause(feature, "e"), params);

        long requests = queryLong(countEventsSql("REQUEST", feature), params);
        long errors = queryLong(countEventsSql("ERROR", feature), params);
        long logins = queryLong(countEventsSql("LOGIN", feature), params);
        long chatMessages = queryLong(countEventsSql("CHAT_MESSAGE", feature), params);
        long documentsUploaded = queryLong(countEventsSql("DOCUMENT_UPLOAD", feature), params);
        long slowRequests = queryLong("""
                SELECT COUNT(*)
                FROM analytics_events e
                WHERE e.created_at >= :from
                  AND e.created_at < :toExclusive
                  AND e.event_type = 'REQUEST'
                  AND e.is_slow = TRUE
                """ + featureFilterClause(feature, "e"), params);

        return new AnalyticsOverviewResponse(
                registeredUsers,
                activeUsers,
                requests,
                errors,
                logins,
                chatMessages,
                documentsUploaded,
                slowRequests
        );
    }

    @Transactional(readOnly = true)
    public List<AnalyticsSeriesPoint> timeSeries(AnalyticsMetricType metric, LocalDate from, LocalDate to, String feature) {
        var range = normalizeRange(from, to);
        var params = params(range.fromInstant(), range.toExclusive(), feature);

        String sql = switch (metric) {
            case REQUESTS -> dailyEventSql("REQUEST", feature);
            case ERRORS -> dailyEventSql("ERROR", feature);
            case LOGINS -> dailyEventSql("LOGIN", feature);
            case CHAT_MESSAGES -> dailyEventSql("CHAT_MESSAGE", feature);
            case DOCUMENTS_UPLOADED -> dailyEventSql("DOCUMENT_UPLOAD", feature);
            case SLOW_REQUESTS -> """
                    SELECT CAST(e.created_at AS DATE) AS day, COUNT(*) AS value
                    FROM analytics_events e
                    WHERE e.created_at >= :from
                      AND e.created_at < :toExclusive
                      AND e.event_type = 'REQUEST'
                      AND e.is_slow = TRUE
                    """ + featureFilterClause(feature, "e") + """
                    GROUP BY CAST(e.created_at AS DATE)
                    ORDER BY day
                    """;
            case ACTIVE_USERS -> """
                    SELECT CAST(e.created_at AS DATE) AS day, COUNT(DISTINCT e.user_email) AS value
                    FROM analytics_events e
                    WHERE e.created_at >= :from
                      AND e.created_at < :toExclusive
                      AND e.user_email IS NOT NULL
                      AND e.event_type IN ('REQUEST', 'LOGIN', 'CHAT_MESSAGE', 'DOCUMENT_UPLOAD', 'ERROR')
                    """ + featureFilterClause(feature, "e") + """
                    GROUP BY CAST(e.created_at AS DATE)
                    ORDER BY day
                    """;
            case NEW_USERS -> """
                    SELECT CAST(u.created_at AS DATE) AS day, COUNT(*) AS value
                    FROM users u
                    WHERE u.created_at >= :from
                      AND u.created_at < :toExclusive
                    GROUP BY CAST(u.created_at AS DATE)
                    ORDER BY day
                    """;
        };

        var rawRows = jdbc.query(sql, params, (rs, rowNum) -> new AnalyticsSeriesPoint(
                toLocalDate(rs, "day"),
                rs.getLong("value")
        ));

        return fillMissingDays(range.from(), range.toExclusiveDate(), rawRows);
    }

    @Transactional(readOnly = true)
    public List<EndpointStatsResponse> topEndpoints(LocalDate from, LocalDate to, String feature, int limit) {
        var range = normalizeRange(from, to);
        var params = params(range.fromInstant(), range.toExclusive(), feature);
        params.addValue("limit", limit);

        var sql = """
                SELECT e.service_name AS service_name,
                       COALESCE(e.method, '-') AS method,
                       COALESCE(e.endpoint, '/') AS endpoint,
                       COUNT(*) AS value
                FROM analytics_events e
                WHERE e.created_at >= :from
                  AND e.created_at < :toExclusive
                  AND e.event_type = 'REQUEST'
                """ + featureFilterClause(feature, "e") + """
                GROUP BY e.service_name, COALESCE(e.method, '-'), COALESCE(e.endpoint, '/')
                ORDER BY value DESC, endpoint ASC
                LIMIT :limit
                """;

        return jdbc.query(sql, params, (rs, rowNum) -> new EndpointStatsResponse(
                rs.getString("service_name"),
                rs.getString("method"),
                rs.getString("endpoint"),
                rs.getLong("value")
        ));
    }

    @Transactional(readOnly = true)
    public List<TopUserResponse> topUsersByRequests(LocalDate from, LocalDate to, String feature, int limit) {
        var range = normalizeRange(from, to);
        var params = params(range.fromInstant(), range.toExclusive(), feature);
        params.addValue("limit", limit);

        var sql = """
                SELECT e.user_email AS email,
                       COALESCE(u.name, e.user_email) AS name,
                       COUNT(*) AS value
                FROM analytics_events e
                LEFT JOIN users u ON u.email = e.user_email
                WHERE e.created_at >= :from
                  AND e.created_at < :toExclusive
                  AND e.event_type = 'REQUEST'
                  AND e.user_email IS NOT NULL
                """ + featureFilterClause(feature, "e") + """
                GROUP BY e.user_email, COALESCE(u.name, e.user_email)
                ORDER BY value DESC, email ASC
                LIMIT :limit
                """;

        return jdbc.query(sql, params, (rs, rowNum) -> new TopUserResponse(
                rs.getString("email"),
                rs.getString("name"),
                rs.getLong("value")
        ));
    }

    private String dailyEventSql(String eventType, String feature) {
        return "SELECT CAST(e.created_at AS DATE) AS day, COUNT(*) AS value "
                + "FROM analytics_events e "
                + "WHERE e.created_at >= :from "
                + "AND e.created_at < :toExclusive "
                + "AND e.event_type = '" + eventType + "' "
                + featureFilterClause(feature, "e")
                + "GROUP BY CAST(e.created_at AS DATE) "
                + "ORDER BY day";
    }

    private String countEventsSql(String eventType, String feature) {
        return "SELECT COUNT(*) "
                + "FROM analytics_events e "
                + "WHERE e.created_at >= :from "
                + "AND e.created_at < :toExclusive "
                + "AND e.event_type = '" + eventType + "' "
                + featureFilterClause(feature, "e");
    }

    private MapSqlParameterSource params(Instant from, Instant toExclusive, String feature) {
        var params = new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from))
                .addValue("toExclusive", Timestamp.from(toExclusive));
        if (feature != null && !feature.isBlank() && !DEFAULT_FEATURE_FILTER.equalsIgnoreCase(feature)) {
            params.addValue("feature", feature);
        }
        return params;
    }

    private String featureFilterClause(String feature, String alias) {
        if (feature == null || feature.isBlank() || DEFAULT_FEATURE_FILTER.equalsIgnoreCase(feature)) {
            return "";
        }
        return "\n  AND " + alias + ".feature = :feature\n";
    }

    private long queryLong(String sql, MapSqlParameterSource params) {
        Long value = jdbc.queryForObject(sql, params, Long.class);
        return value == null ? 0L : value;
    }

    private LocalDate toLocalDate(ResultSet rs, String column) throws SQLException {
        var date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }

    private List<AnalyticsSeriesPoint> fillMissingDays(LocalDate from, LocalDate toExclusive, List<AnalyticsSeriesPoint> rows) {
        var values = new LinkedHashMap<LocalDate, Long>();
        for (var row : rows) {
            values.put(row.day(), row.value());
        }

        var result = new ArrayList<AnalyticsSeriesPoint>();
        var current = from;
        while (current.isBefore(toExclusive)) {
            result.add(new AnalyticsSeriesPoint(current, values.getOrDefault(current, 0L)));
            current = current.plusDays(1);
        }
        return result;
    }

    private Range normalizeRange(LocalDate from, LocalDate to) {
        var end = to != null ? to : LocalDate.now(ZoneOffset.UTC);
        var start = from != null ? from : end.minusDays(29);
        if (start.isAfter(end)) {
            var swap = start;
            start = end;
            end = swap;
        }
        return new Range(
                start,
                end.plusDays(1),
                start.atStartOfDay().toInstant(ZoneOffset.UTC),
                end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }

    private record Range(LocalDate from, LocalDate toExclusiveDate, Instant fromInstant, Instant toExclusive) {}
}
