package com.playtomic.tests.wallet.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class WalletEntity {

    @Id
    private String id;

    private Long balanceAmountUnit;

    public WalletEntity() {
    }

    public WalletEntity(String id, Long balanceAmountUnit) {
        this.id = id;
        this.balanceAmountUnit = balanceAmountUnit;
    }

}
