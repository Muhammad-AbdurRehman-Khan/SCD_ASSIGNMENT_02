package com.uet.billing;


/** Result of a payment attempt: a status plus the message that should be shown to the user. */
public final class PaymentResult {

    private final PaymentStatus status;
    private final String message;

    private PaymentResult(PaymentStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static PaymentResult paid(String message) {
        return new PaymentResult(PaymentStatus.PAID, message);
    }

    public static PaymentResult pendingCash(String message) {
        return new PaymentResult(PaymentStatus.PENDING_CASH, message);
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(PaymentStatus.FAILED, message);
    }

    /** @return true when the order may continue (paid, or cash will be collected later) */
    public boolean isApproved() {
        return status != PaymentStatus.FAILED;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    /** @return the text to print; empty when the payment method has nothing to say */
    public String getMessage() {
        return message;
    }
}
