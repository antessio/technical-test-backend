package com.playtomic.tests.wallet.service;

import java.util.Optional;

import com.playtomic.tests.wallet.service.domain.Lock;

public interface WalletLockRepository {

    void insertLock(Lock lock);

    void deleteLock(String name);

    Optional<Lock> getLock(String name);

}
