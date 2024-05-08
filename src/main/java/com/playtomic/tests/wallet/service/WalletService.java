package com.playtomic.tests.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.service.domain.CreditCard;
import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletId;
import com.playtomic.tests.wallet.service.domain.WalletTopUpCommand;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;

@Service
public class WalletService {

    private final StripeService stripeService;
    private final WalletLockService walletLockService;
    private final WalletRepository walletRepository;

    public WalletService(
            WalletRepository walletRepository,
            StripeService stripeService,
            WalletLockService walletLockService) {
        this.walletRepository = walletRepository;
        this.stripeService = stripeService;
        this.walletLockService = walletLockService;
    }


    public Optional<Wallet> getWallet(WalletId walletId) {
        return walletRepository.getWallet(walletId);
    }

    public Wallet createWallet(){
        Wallet wallet = new Wallet(
                new WalletId(UUID.randomUUID().toString()),
                0L
        );

        walletRepository.insertWallet(wallet);
        return wallet;
    }
    public void topUpWallet(WalletTopUpCommand walletTopUpCommand) {
        Wallet wallet = getWallet(walletTopUpCommand.walletId())
                .orElseThrow(() -> new IllegalArgumentException("wallet must exist"));
        // lock the wallet
        walletLockService.lock(wallet.getId());
        boolean charged = chargeOnExternalProvider(walletTopUpCommand);
        if (charged) {
            updateWalletBalance(wallet, walletTopUpCommand.monetaryAmount().amountUnit());
        }
        // unlock the wallet
        walletLockService.unlock(wallet.getId());
    }

    private boolean chargeOnExternalProvider(WalletTopUpCommand walletTopUpCommand) {
        if (walletTopUpCommand.paymentMethod() instanceof CreditCard creditCard) {
            try {
                stripeService.charge(
                        creditCard.getCardNumber(),
                        new BigDecimal(walletTopUpCommand.monetaryAmount().amountUnit()).divide(new BigDecimal(100), RoundingMode.UNNECESSARY));
                return true;
            } catch (StripeServiceException e) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("payment method not supported");
        }
    }

    private void updateWalletBalance(Wallet wallet, Long deltaAmountUnit) {
        wallet.updateWalletBalance(deltaAmountUnit);
        walletRepository.updateWallet(wallet);
    }
}
