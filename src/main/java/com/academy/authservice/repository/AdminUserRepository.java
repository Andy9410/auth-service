package com.academy.authservice.repository;

import com.academy.authservice.dto.AdminUserFilters;
import com.academy.authservice.dto.AdminUserPageResponse;
import com.academy.authservice.dto.AdminUserSummaryResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class AdminUserRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AdminUserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AdminUserPageResponse findPage(AdminUserFilters filters, int page, int size) {
        var params = new MapSqlParameterSource()
                .addValue("email", normalizeLike(filters.email()), Types.VARCHAR)
                .addValue("name", normalizeLike(filters.name()), Types.VARCHAR)
                .addValue("limit", size)
                .addValue("offset", Math.max(page, 0) * size);

        String where = """
                WHERE (:email IS NULL OR LOWER(u.email) LIKE :email)
                  AND (:name IS NULL OR LOWER(u.name) LIKE :name)
                """;

        String baseFrom = """
                FROM users u
                LEFT JOIN roles r ON r.id = u.role_id
                LEFT JOIN (
                    SELECT e.user_email,
                           MAX(e.created_at) AS last_login_at
                    FROM analytics_events e
                    WHERE e.event_type = 'LOGIN'
                    GROUP BY e.user_email
                ) logins ON logins.user_email = u.email
                """;

        String sql = """
                SELECT u.id,
                       u.name,
                       u.email,
                       r.name AS role,
                       u.created_at,
                       logins.last_login_at,
                       CASE
                         WHEN logins.last_login_at >= NOW() - INTERVAL '30 days' THEN TRUE
                         ELSE FALSE
                       END AS active
                """ + baseFrom + where + """
                ORDER BY COALESCE(logins.last_login_at, u.created_at) DESC, u.id DESC
                LIMIT :limit OFFSET :offset
                """;

        String countSql = "SELECT COUNT(*) " + baseFrom + where;

        List<AdminUserSummaryResponse> content = jdbc.query(sql, params, (rs, _rowNum) -> new AdminUserSummaryResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("role"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("last_login_at")),
                rs.getBoolean("active")
        ));

        long total = Optional.ofNullable(jdbc.queryForObject(countSql, params, Long.class)).orElse(0L);
        int totalPages = size <= 0 ? 1 : (int) Math.max(1, Math.ceil((double) total / size));
        return new AdminUserPageResponse(content, total, totalPages, page, size);
    }

    private String normalizeLike(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private Instant toInstant(Timestamp value) {
        return value != null ? value.toInstant() : null;
    }
}
