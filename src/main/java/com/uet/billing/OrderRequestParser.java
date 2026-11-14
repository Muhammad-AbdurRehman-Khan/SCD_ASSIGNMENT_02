package com.uet.billing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates the raw order map and turns it into a typed {@link OrderRequest}.
 * This is the ONE place that still knows the map keys and does the casts.
 */
public class OrderRequestParser {

    private static final String DEFAULT_CUSTOMER_TYPE = "REGULAR";
    private static final String DEFAULT_COUNTRY = "PK";
    private static final String DEFAULT_PAYMENT_METHOD = "CASH";
    private static final String DEFAULT_NOTIFY_CHANNEL = "EMAIL";
    private static final String DEFAULT_PHONE = "0300-0000000";

    /** @throws InvalidOrderException with a user-readable message when the order is not acceptable */
    public OrderRequest parse(Map<String, Object> orderData) {
        if (orderData == null || orderData.isEmpty()) {
            throw new InvalidOrderException("Error: Order data is empty!");
        }

        String email = (String) orderData.get("customerEmail");
        if (email == null || !email.contains("@")) {
            throw new InvalidOrderException("Error: Invalid customer email!");
        }

        List<OrderItem> items = parseItems(orderData.get("items"));

        return new OrderRequest.Builder()
                .customerEmail(email)
                .items(items)
                .customerType(CustomerType.from((String) orderData.getOrDefault("customerType", DEFAULT_CUSTOMER_TYPE)))
                .country((String) orderData.getOrDefault("country", DEFAULT_COUNTRY))
                .paymentMethod((String) orderData.getOrDefault("paymentMethod", DEFAULT_PAYMENT_METHOD))
                .cardNumber((String) orderData.getOrDefault("cardNumber", ""))
                .mobileNumber((String) orderData.getOrDefault("mobileNumber", ""))
                .notifyChannel((String) orderData.getOrDefault("notifyChannel", DEFAULT_NOTIFY_CHANNEL))
                .phone((String) orderData.getOrDefault("phone", DEFAULT_PHONE))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<OrderItem> parseItems(Object rawItems) {
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) rawItems;
        if (itemMaps == null || itemMaps.isEmpty()) {
            throw new InvalidOrderException("Error: Cart has no items!");
        }

        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemMap : itemMaps) {
            items.add(parseItem(itemMap));
        }
        return items;
    }

    private OrderItem parseItem(Map<String, Object> itemMap) {
        if (!itemMap.containsKey("price") || !itemMap.containsKey("quantity")) {
            throw new InvalidOrderException("Error: Missing item price or quantity!");
        }
        double price = (double) itemMap.get("price");
        int quantity = (int) itemMap.get("quantity");
        if (price <= 0 || quantity <= 0) {
            throw new InvalidOrderException("Error: Invalid price or quantity for item!");
        }
        return new OrderItem(price, quantity);
    }
}
