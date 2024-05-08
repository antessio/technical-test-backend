package com.playtomic.tests.wallet.service.domain;

public class CreditCard implements PaymentMethod{
    private final String cardNumber;


    public CreditCard(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

}
