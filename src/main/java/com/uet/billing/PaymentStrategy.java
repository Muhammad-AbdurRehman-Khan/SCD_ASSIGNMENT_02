package com.uet.billing;

/**
 * Strategy pattern: one implementation per payment method.
 * To support a new method (e.g. JazzCash) add a new class - nothing existing has to change.
 */
public interface PaymentStrategy {

    /** The value of the order's "paymentMethod" field this strategy handles, e.g. "CREDIT_CARD". */
    String methodName();

    PaymentResult pay(PaymentRequest request);
}
