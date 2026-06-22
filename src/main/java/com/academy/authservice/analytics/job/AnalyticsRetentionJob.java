package com.academy.authservice.analytics.job;

import com.academy.authservice.analytics.repository.AnalyticsEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class AnalyticsRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsRetentionJob.class);

    private final AnalyticsEventRepository analyticsEventRepository;
    private final int retentionDays;

    public AnalyticsRetentionJob(AnalyticsEventRepository analyticsEventRepository,
                                 @Value("${analytics.retention-days:30}") int retentionDays) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${analytics.retention-cron:0 30 3 * * *}", zone = "UTC")
    @Transactional
    public void purgeExpiredEvents() {
        if (retentionDays <= 0) {
            return;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = analyticsEventRepository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Deleted {} analytics events older than {} days", deleted, retentionDays);
        }
    }
}
