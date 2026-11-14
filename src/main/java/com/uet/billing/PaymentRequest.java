package com.uet.billing;

/**
 * The only data a payment method needs: how much to charge and the customer's payment details.
 * (Interface Segregation: a payment strategy does not get to see the whole order.)
 */
public class PaymentRequest {

    private final double amount;
    private final String cardNumber;
    private final String mobileNumber;

    public PaymentRequest(double amount, String cardNumber, String mobileNumber) {
        this.amount = amount;
        this.cardNumber = cardNumber;
        this.mobileNumber = mobileNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
}
