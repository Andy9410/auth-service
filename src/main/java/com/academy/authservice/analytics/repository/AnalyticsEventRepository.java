package com.academy.authservice.analytics.repository;

import com.academy.authservice.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
}
