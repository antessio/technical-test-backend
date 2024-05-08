package com.playtomic.tests.wallet.service.domain;

import java.time.Instant;

public class Lock {
    private final String name;
    private final Instant expiresAt;

    public Lock(String name, Instant expiresAt) {
        this.name = name;
        this.expiresAt = expiresAt;
    }

    public String getName() {
        return name;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

}
