package com.uet.billing;

import java.util.ArrayList;
import java.util.List;

/**
 * A validated, typed order. Replaces the untyped {@code Map<String, Object>} of the legacy code.
 * Create it with {@link Builder}.
 */
public class OrderRequest {

    private final String customerEmail;
    private final List<OrderItem> items;
    private final CustomerType customerType;
    private final String country;
    private final String paymentMethod;
    private final String cardNumber;
    private final String mobileNumber;
    private final String notifyChannel;
    private final String phone;

    private OrderRequest(Builder builder) {
        this.customerEmail = builder.customerEmail;
        this.items = new ArrayList<>(builder.items);
        this.customerType = builder.customerType;
        this.country = builder.country;
        this.paymentMethod = builder.paymentMethod;
        this.cardNumber = builder.cardNumber;
        this.mobileNumber = builder.mobileNumber;
        this.notifyChannel = builder.notifyChannel;
        this.phone = builder.phone;
    }

    /** @return price * quantity summed over all items, before discount and tax */
    public double subtotal() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.lineTotal();
        }
        return total;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public String getCountry() {
        return country;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getNotifyChannel() {
        return notifyChannel;
    }

    public String getPhone() {
        return phone;
    }

    public static class Builder {
        private String customerEmail;
        private List<OrderItem> items = new ArrayList<>();
        private CustomerType customerType = CustomerType.REGULAR;
        private String country;
        private String paymentMethod;
        private String cardNumber;
        private String mobileNumber;
        private String notifyChannel;
        private String phone;

        public Builder customerEmail(String value) {
            this.customerEmail = value;
            return this;
        }

        public Builder items(List<OrderItem> value) {
            this.items = value;
            return this;
        }

        public Builder customerType(CustomerType value) {
            this.customerType = value;
            return this;
        }

        public Builder country(String value) {
            this.country = value;
            return this;
        }

        public Builder paymentMethod(String value) {
            this.paymentMethod = value;
            return this;
        }

        public Builder cardNumber(String value) {
            this.cardNumber = value;
            return this;
        }

        public Builder mobileNumber(String value) {
            this.mobileNumber = value;
            return this;
        }

        public Builder notifyChannel(String value) {
            this.notifyChannel = value;
            return this;
        }

        public Builder phone(String value) {
            this.phone = value;
            return this;
        }

        public OrderRequest build() {
            return new OrderRequest(this);
        }
    }
}
