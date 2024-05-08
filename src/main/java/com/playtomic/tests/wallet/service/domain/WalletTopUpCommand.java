package com.playtomic.tests.wallet.service.domain;

public record WalletTopUpCommand(WalletId walletId,
                                 MonetaryAmount monetaryAmount,
                                 PaymentMethod paymentMethod) {

}
