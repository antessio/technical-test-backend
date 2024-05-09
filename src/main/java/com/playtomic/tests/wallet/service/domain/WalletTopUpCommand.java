package com.playtomic.tests.wallet.service.domain;

public record WalletTopUpCommand(Long amountUnit,
                                 String creditCardNumber) {

}
