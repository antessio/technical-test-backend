package com.playtomic.tests.wallet.service;

import java.util.Optional;

import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletId;

public interface WalletRepository {
    void insertWallet(Wallet wallet);

    Optional<Wallet> getWallet(WalletId walletId);

    void updateWallet(Wallet wallet);

}
