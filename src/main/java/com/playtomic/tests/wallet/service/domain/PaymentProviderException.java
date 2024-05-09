package com.playtomic.tests.wallet.service.domain;

import com.playtomic.tests.wallet.service.stripe.StripeServiceException;

public class PaymentProviderException extends WalletException{

    public PaymentProviderException(String message) {
        super(message);
    }

    public PaymentProviderException(Throwable e) {
        super(e);
    }

    public PaymentProviderException(String message, Throwable e) {
        super(message, e);
    }

}
