package com.uet.billing;

/** Confirms the order by e-mail (simulated on the console). */
public class EmailNotificationService implements NotificationService {

    @Override
    public String channel() {
        return "EMAIL";
    }

    @Override
    public void send(NotificationRequest request) {
        System.out.println("[EMAIL DISPATCHED] To: " + request.getEmail() + " | Your order of PKR "
                + String.format("%.2f", request.getAmount()) + " is confirmed!");
    }
}
