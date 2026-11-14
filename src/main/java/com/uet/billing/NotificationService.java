package com.uet.billing;

/**
 * One implementation per way of telling the customer that the order is confirmed.
 * To add WhatsApp, write a new class that implements this interface.
 */
public interface NotificationService {

    /** The value of the order's "notifyChannel" field this service handles, e.g. "EMAIL". */
    String channel();

    void send(NotificationRequest request);
}
