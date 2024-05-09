package com.playtomic.tests.wallet.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.persistence.SpringDataWalletRepository;
import com.playtomic.tests.wallet.persistence.WalletEntity;
import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletId;

@Service
public class WalletRepositoryAdapter implements WalletRepository{
    private final SpringDataWalletRepository walletRepository;

    public WalletRepositoryAdapter(SpringDataWalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void insertWallet(Wallet wallet) {
        walletRepository.save(new WalletEntity(wallet.getId().id(), wallet.getBalanceAmountUnit()));
    }

    @Override
    public Optional<Wallet> getWallet(WalletId walletId) {
        return walletRepository.findById(walletId.id())
                .map(w -> new Wallet(new WalletId(w.getId()), w.getBalanceAmountUnit()));
    }

    @Override
    public void updateWallet(Wallet wallet) {
        walletRepository.save(new WalletEntity(
                wallet.getId().id(),
                wallet.getBalanceAmountUnit()
        ));
    }

}
