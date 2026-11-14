package com.uet.billing;

/** What a notification channel needs to know: who to contact and how much was paid. */
public class NotificationRequest {

    private final String email;
    private final String phone;
    private final double amount;

    public NotificationRequest(String email, String phone, double amount) {
        this.email = email;
        this.phone = phone;
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public double getAmount() {
        return amount;
    }
}
