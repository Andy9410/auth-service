package com.academy.authservice.analytics.repository;

import com.academy.authservice.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    @Modifying
    @Query("delete from AnalyticsEvent e where e.createdAt < :cutoff")
    long deleteByCreatedAtBefore(Instant cutoff);
}
