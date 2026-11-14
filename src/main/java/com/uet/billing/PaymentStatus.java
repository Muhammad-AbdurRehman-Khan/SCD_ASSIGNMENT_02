package com.uet.billing;

/** Outcome of a payment attempt. Replaces the strings "PAID", "PENDING_CASH" and "FAILED". */
public enum PaymentStatus {
    PAID,
    PENDING_CASH,
    FAILED
}
