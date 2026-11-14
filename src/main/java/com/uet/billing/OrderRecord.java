package com.uet.billing;

import java.time.LocalDateTime;

/** What the system remembers about a processed order (one line of the audit log). */
public class OrderRecord {

    private final String customerEmail;
    private final double payableAmount;
    private final PaymentStatus paymentStatus;
    private final LocalDateTime createdAt;

    public OrderRecord(String customerEmail, double payableAmount,
                       PaymentStatus paymentStatus, LocalDateTime createdAt) {
        this.customerEmail = customerEmail;
        this.payableAmount = payableAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public double getPayableAmount() {
        return payableAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
