package com.uet.billing;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates one order from start to finish. It owns no business rule itself:
 * parse -> discount -> tax -> pay -> save -> notify, each step delegated to a collaborator.
 * All collaborators are passed in (Dependency Inversion), so any of them can be replaced.
 */
public class OrderService {

    private final OrderRequestParser parser;
    private final DiscountCalculator discountCalculator;
    private final TaxCalculator taxCalculator;
    private final OrderRepository orderRepository;
    private final Map<String, PaymentStrategy> paymentStrategies = new HashMap<>();
    private final Map<String, NotificationService> notificationServices = new HashMap<>();

    public OrderService(OrderRequestParser parser,
                        DiscountCalculator discountCalculator,
                        TaxCalculator taxCalculator,
                        List<PaymentStrategy> paymentStrategies,
                        OrderRepository orderRepository,
                        List<NotificationService> notificationServices) {
        this.parser = parser;
        this.discountCalculator = discountCalculator;
        this.taxCalculator = taxCalculator;
        this.orderRepository = orderRepository;
        for (PaymentStrategy strategy : paymentStrategies) {
            this.paymentStrategies.put(strategy.methodName(), strategy);
        }
        for (NotificationService service : notificationServices) {
            this.notificationServices.put(service.channel(), service);
        }
    }

    /** @return true when the order was accepted and processed, false when it was rejected */
    public boolean processOrder(Map<String, Object> orderData) {
        OrderRequest order;
        try {
            order = parser.parse(orderData);
        } catch (InvalidOrderException e) {
            System.out.println(e.getMessage());
            return false;
        }

        double payableAmount = calculatePayableAmount(order);

        PaymentResult paymentResult = pay(order, payableAmount);
        if (paymentResult == null || !paymentResult.isApproved()) {
            return false;
        }

        save(order, payableAmount, paymentResult);
        notifyCustomer(order, payableAmount);
        return true;
    }

    private double calculatePayableAmount(OrderRequest order) {
        double subtotal = order.subtotal();
        double discount = discountCalculator.calculate(order.getCustomerType(), subtotal, order.getItems().size());
        double netAmount = subtotal - discount;
        double tax = taxCalculator.calculate(order.getCountry(), netAmount);
        return netAmount + tax;
    }

    /** @return the payment result, or null when the payment method is not supported */
    private PaymentResult pay(OrderRequest order, double payableAmount) {
        PaymentStrategy strategy = paymentStrategies.get(order.getPaymentMethod());
        if (strategy == null) {
            System.out.println("Error: Unsupported payment method: " + order.getPaymentMethod());
            return null;
        }
        PaymentResult result = strategy.pay(
                new PaymentRequest(payableAmount, order.getCardNumber(), order.getMobileNumber()));
        if (!result.getMessage().isEmpty()) {
            System.out.println(result.getMessage());
        }
        return result;
    }

    private void save(OrderRequest order, double payableAmount, PaymentResult paymentResult) {
        try {
            orderRepository.save(new OrderRecord(
                    order.getCustomerEmail(), payableAmount, paymentResult.getStatus(), LocalDateTime.now()));
        } catch (IOException e) {
            System.out.println("Failed to write order log: " + e.getMessage());
        }
    }

    private void notifyCustomer(OrderRequest order, double payableAmount) {
        NotificationService service = notificationServices.get(order.getNotifyChannel());
        if (service != null) {
            service.send(new NotificationRequest(order.getCustomerEmail(), order.getPhone(), payableAmount));
        }
    }
}
