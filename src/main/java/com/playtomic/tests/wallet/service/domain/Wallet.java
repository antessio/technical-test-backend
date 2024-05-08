package com.playtomic.tests.wallet.service.domain;

import lombok.Getter;

@Getter
public class Wallet {

    private WalletId id;
    private Long balanceAmountUnit;

    public Wallet(WalletId id, Long balanceAmountUnit) {
        this.id = id;
        this.balanceAmountUnit = balanceAmountUnit;
    }

    public void updateWalletBalance(Long deltaAmountUnit){
        this.balanceAmountUnit+=deltaAmountUnit;
    }

}
