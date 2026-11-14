package com.uet.billing;

/** Regular customers: flat PKR 500 off for orders above PKR 10,000, otherwise nothing. */
public class RegularDiscountPolicy implements DiscountPolicy {

    private static final double LARGE_ORDER_THRESHOLD = 10_000;
    private static final double LARGE_ORDER_FLAT_DISCOUNT = 500.0;

    @Override
    public double discountFor(double subtotal, int lineItemCount) {
        return subtotal > LARGE_ORDER_THRESHOLD ? LARGE_ORDER_FLAT_DISCOUNT : 0.0;
    }
}
