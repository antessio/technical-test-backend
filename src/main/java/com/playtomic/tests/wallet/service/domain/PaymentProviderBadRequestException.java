package com.playtomic.tests.wallet.service.domain;


public class PaymentProviderBadRequestException extends WalletException{

    public PaymentProviderBadRequestException(String message, Throwable e) {
        super(message, e);
    }

}
