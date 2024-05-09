package com.playtomic.tests.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.playtomic.tests.wallet.service.domain.PaymentProviderBadRequestException;
import com.playtomic.tests.wallet.service.domain.PaymentProviderException;
import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletId;
import com.playtomic.tests.wallet.service.domain.WalletTopUpCommand;
import com.playtomic.tests.wallet.service.stripe.StripeAmountTooSmallException;
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
    public void topUpWallet(WalletId walletId, WalletTopUpCommand walletTopUpCommand) {
        Wallet wallet = getWallet(walletId)
                .orElseThrow(() -> new IllegalArgumentException("wallet must exist"));
        // lock the wallet
        walletLockService.lock(wallet.getId());
        try {
            chargeOnExternalProvider(walletTopUpCommand);
            updateWalletBalance(wallet, walletTopUpCommand.amountUnit());
        } finally {
            // unlock the wallet
            walletLockService.unlock(wallet.getId());
        }
    }

    private void chargeOnExternalProvider(WalletTopUpCommand walletTopUpCommand) {
            try {
                stripeService.charge(
                        walletTopUpCommand.creditCardNumber(),
                        new BigDecimal(walletTopUpCommand.amountUnit()).divide(new BigDecimal(100), RoundingMode.UNNECESSARY));
            } catch (StripeAmountTooSmallException e) {
                throw new PaymentProviderBadRequestException("top-up amount is too small", e);
            } catch (StripeServiceException e) {
                throw new PaymentProviderException("error with payment provider", e);
            }
    }

    private void updateWalletBalance(Wallet wallet, Long deltaAmountUnit) {
        wallet.updateWalletBalance(deltaAmountUnit);
        walletRepository.updateWallet(wallet);
    }
}
