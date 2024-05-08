package com.playtomic.tests.wallet.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.persistence.LockEntity;
import com.playtomic.tests.wallet.persistence.SpringDataLockRepository;
import com.playtomic.tests.wallet.service.domain.Lock;

@Service
public class WalletLockRepositoryAdapter implements WalletLockRepository {

    private final SpringDataLockRepository springDataLockRepository;

    public WalletLockRepositoryAdapter(SpringDataLockRepository springDataLockRepository) {
        this.springDataLockRepository = springDataLockRepository;
    }

    @Override
    public void upsertLock(Lock lock) {
        springDataLockRepository.save(new LockEntity(
                lock.getName(),
                lock.getExpiresAt()
        ));
    }

    @Override
    public void deleteLock(String name) {
        springDataLockRepository.deleteById(name);
    }

    @Override
    public Optional<Lock> getLock(String name) {
        return springDataLockRepository.findById(name)
                                       .map(l -> new Lock(l.getName(), l.getExpiresAt()));
    }

}
