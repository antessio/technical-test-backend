package com.playtomic.tests.wallet.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.service.domain.Lock;
import com.playtomic.tests.wallet.service.domain.WalletId;

@Service
public class WalletLockService {
    private static final String WALLET_LOCK_FORMAT = "WALLET#%s";
    private static final TemporalAmount LOCK_EXPIRATION = Duration.ofMinutes(5);
    private final WalletLockRepository walletLockRepository;

    public WalletLockService(WalletLockRepository walletLockRepository) {
        this.walletLockRepository = walletLockRepository;
    }

    public void lock(WalletId walletId){
        String lockName = getLockName(walletId);
        Instant now = Instant.now();
        boolean isLocked = walletLockRepository.getLock(lockName)
                                               .map(l -> l.getExpiresAt().isAfter(now))
                                               .orElse(false);
        if (isLocked) {
            throw new IllegalStateException("%s is locked".formatted(walletId.id()));
        }
        walletLockRepository.upsertLock(new Lock(
                lockName,
                now.plus(LOCK_EXPIRATION)
        ));

    }

    public void unlock(WalletId walletId) {
        walletLockRepository.deleteLock(getLockName(walletId));
    }

    private String getLockName(WalletId walletId) {
        return WALLET_LOCK_FORMAT.formatted(walletId.id());
    }

}
