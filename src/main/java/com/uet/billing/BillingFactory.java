package com.uet.billing;

import java.util.List;

/**
 * The "composition root": the single place that decides which concrete classes are used.
 * Registering a new payment method or notification channel is one line in the lists below.
 */
public final class BillingFactory {

    private BillingFactory() {
    }

    public static List<PaymentStrategy> defaultPaymentStrategies() {
        return List.of(
                new CreditCardPayment(),
                new EasyPaisaPayment(),
                new CashOnDeliveryPayment());
    }

    public static List<NotificationService> defaultNotificationServices() {
        return List.of(
                new EmailNotificationService(),
                new SmsNotificationService());
    }

    public static OrderService createDefault(String logFilePath) {
        return new OrderService(
                new OrderRequestParser(),
                new DiscountCalculator(),
                new TaxCalculator(),
                defaultPaymentStrategies(),
                new FileOrderRepository(logFilePath),
                defaultNotificationServices());
    }
}
