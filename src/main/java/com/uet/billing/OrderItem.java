package com.uet.billing;

/** One line of the cart: a unit price and how many units were ordered. */
public class OrderItem {

    private final double price;
    private final int quantity;

    public OrderItem(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double lineTotal() {
        return price * quantity;
    }
}
