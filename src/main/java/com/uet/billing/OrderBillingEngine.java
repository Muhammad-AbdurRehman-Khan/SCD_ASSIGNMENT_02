package com.uet.billing;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LEGACY BILLING ENGINE
 * 
 * NOTICE TO STUDENTS:
 * This class works and compiles, but its design is messy, hard to test, and tightly coupled.
 * Do NOT start modifying this file directly. First, write your JUnit 5 safety tests.
 */
public class OrderBillingEngine {

    // Bad Practice 1: Hardcoded file paths and direct file manipulation
    private String logFilePath = "order_audit.log";
    private OrderRepository orderRepository = new FileOrderRepository(logFilePath);

    private final DiscountCalculator discountCalculator = new DiscountCalculator();
    private final TaxCalculator taxCalculator = new TaxCalculator();
    private final Map<String, PaymentStrategy> paymentStrategies = new HashMap<>();
    private final Map<String, NotificationService> notificationServices = new HashMap<>();

    public OrderBillingEngine() {
        for (PaymentStrategy strategy : List.of(
                new CreditCardPayment(), new EasyPaisaPayment(), new CashOnDeliveryPayment())) {
            paymentStrategies.put(strategy.methodName(), strategy);
        }
        for (NotificationService service : List.of(new EmailNotificationService(), new SmsNotificationService())) {
            notificationServices.put(service.channel(), service);
        }
    }

    public boolean processOrder(Map<String, Object> orderData) {
        // 1. Input Validation (Messy nested validation and console error printing)
        if (orderData == null || orderData.isEmpty()) {
            System.out.println("Error: Order data is empty!");
            return false;
        }

        String email = (String) orderData.get("customerEmail");
        if (email == null || !email.contains("@")) {
            System.out.println("Error: Invalid customer email!");
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        if (items == null || items.isEmpty()) {
            System.out.println("Error: Cart has no items!");
            return false;
        }

        // 2. Total & Discount Calculation (Long method, primitive types, magic numbers)
        double rawTotal = 0.0;
        for (Map<String, Object> item : items) {
            if (!item.containsKey("price") || !item.containsKey("quantity")) {
                System.out.println("Error: Missing item price or quantity!");
                return false;
            }
            double price = (double) item.get("price");
            int quantity = (int) item.get("quantity");
            if (price <= 0 || quantity <= 0) {
                System.out.println("Error: Invalid price or quantity for item!");
                return false;
            }
            rawTotal += price * quantity;
        }

        // Step 5: discount and tax rules now live in their own classes
        String customerType = (String) orderData.getOrDefault("customerType", "REGULAR");
        double discount = discountCalculator.calculate(CustomerType.from(customerType), rawTotal, items.size());
        double netAmount = rawTotal - discount;

        String country = (String) orderData.getOrDefault("country", "PK");
        double tax = taxCalculator.calculate(country, netAmount);
        double payableAmount = netAmount + tax;

        // Step 6: every payment method is a PaymentStrategy now
        String paymentMethod = (String) orderData.getOrDefault("paymentMethod", "CASH");
        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethod);
        if (paymentStrategy == null) {
            System.out.println("Error: Unsupported payment method: " + paymentMethod);
            return false;
        }

        PaymentRequest paymentRequest = new PaymentRequest(
                payableAmount,
                (String) orderData.getOrDefault("cardNumber", ""),
                (String) orderData.getOrDefault("mobileNumber", ""));
        PaymentResult paymentResult = paymentStrategy.pay(paymentRequest);
        if (!paymentResult.getMessage().isEmpty()) {
            System.out.println(paymentResult.getMessage());
        }
        if (!paymentResult.isApproved()) {
            return false;
        }
        // Step 8: persistence goes through the OrderRepository interface
        try {
            orderRepository.save(new OrderRecord(email, payableAmount, paymentResult.getStatus(), LocalDateTime.now()));
        } catch (IOException e) {
            System.out.println("Failed to write order log: " + e.getMessage());
        }

        // Step 7: notification channels are NotificationService implementations now
        String notifyChannel = (String) orderData.getOrDefault("notifyChannel", "EMAIL");
        NotificationService notificationService = notificationServices.get(notifyChannel);
        if (notificationService != null) {
            String phone = (String) orderData.getOrDefault("phone", "0300-0000000");
            notificationService.send(new NotificationRequest(email, phone, payableAmount));
        }

        return true;
    }

    // Helper getter/setter for testing file path redirection
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        this.orderRepository = new FileOrderRepository(logFilePath);
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}
