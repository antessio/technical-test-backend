package com.playtomic.tests.wallet.service.domain;

public class WalletException extends RuntimeException{

    public WalletException(String message) {
        super(message);
    }

    public WalletException(Throwable e) {
        super(e);
    }

    public WalletException(String message, Throwable e) {
        super(message, e);
    }

}
