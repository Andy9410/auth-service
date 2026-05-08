package com.academy.authservice.job;

import com.academy.authservice.repository.RevokedTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenCleanupJob {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenCleanupJob(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void deleteExpiredTokens() {
        revokedTokenRepository.deleteExpiredBefore(Instant.now());
    }
}
