package com.uet.billing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Small runnable demo (NetBeans: right-click project -> Run). Writes to order_audit.log. */
public class Demo {

    public static void main(String[] args) {
        OrderService service = BillingFactory.createDefault("order_audit.log");

        System.out.println("--- VIP customer pays by credit card, notified by e-mail");
        service.processOrder(order("VIP", "CREDIT_CARD", "EMAIL", 8000.0, 1));

        System.out.println("--- Student pays by EasyPaisa, notified by SMS");
        service.processOrder(order("STUDENT", "EASYPAISA", "SMS", 1500.0, 2));

        System.out.println("--- Cash on delivery above the PKR 25,000 limit is rejected");
        service.processOrder(order("REGULAR", "CASH_ON_DELIVERY", "EMAIL", 30000.0, 1));
    }

    private static Map<String, Object> order(String customerType, String paymentMethod, String channel,
                                             double price, int quantity) {
        Map<String, Object> item = new HashMap<>();
        item.put("price", price);
        item.put("quantity", quantity);

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item);

        Map<String, Object> order = new HashMap<>();
        order.put("customerEmail", "ali@uet.edu.pk");
        order.put("customerType", customerType);
        order.put("items", items);
        order.put("paymentMethod", paymentMethod);
        order.put("cardNumber", "1234567890123456");
        order.put("mobileNumber", "03001234567");
        order.put("notifyChannel", channel);
        order.put("phone", "0300-1234567");
        return order;
    }
}
