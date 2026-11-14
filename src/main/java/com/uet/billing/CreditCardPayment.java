package com.uet.billing;

/** Credit card: the card number must be exactly 16 digits. */
public class CreditCardPayment implements PaymentStrategy {

    private static final int CARD_NUMBER_LENGTH = 16;

    @Override
    public String methodName() {
        return "CREDIT_CARD";
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        String cardNumber = request.getCardNumber();
        if (cardNumber.length() == CARD_NUMBER_LENGTH && cardNumber.matches("\\d+")) {
            return PaymentResult.paid("Processing credit card payment of PKR " + request.getAmount());
        }
        return PaymentResult.declined("Card declined: Must be 16 digits.");
    }
}
