package com.uet.billing;

/** Confirms the order by SMS (simulated on the console). */
public class SmsNotificationService implements NotificationService {

    @Override
    public String channel() {
        return "SMS";
    }

    @Override
    public void send(NotificationRequest request) {
        System.out.println("[SMS DISPATCHED] To: " + request.getPhone() + " | Order confirmed! Paid: PKR "
                + String.format("%.2f", request.getAmount()));
    }
}
