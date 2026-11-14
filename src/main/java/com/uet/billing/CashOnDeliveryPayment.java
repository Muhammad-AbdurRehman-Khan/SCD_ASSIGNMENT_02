package com.uet.billing;

/** Cash on delivery: allowed up to PKR 25,000 (checked against the amount payable after tax). */
public class CashOnDeliveryPayment implements PaymentStrategy {

    private static final double MAX_AMOUNT = 25_000;

    @Override
    public String methodName() {
        return "CASH_ON_DELIVERY";
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        if (request.getAmount() > MAX_AMOUNT) {
            return PaymentResult.declined("Cash on delivery not allowed for orders above PKR 25,000.");
        }
        return PaymentResult.pendingCash("");
    }
}
