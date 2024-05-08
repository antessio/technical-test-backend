package com.playtomic.tests.wallet.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class LockEntity {

    @Id
    private String name;
    private Instant expiresAt;
    public LockEntity() {

    }

    public LockEntity(String name, Instant expiresAt) {
        this.name = name;
        this.expiresAt = expiresAt;
    }

}
