package com.uet.billing;

/** VIP customers: 15 % for orders above PKR 5,000, otherwise 10 %. */
public class VipDiscountPolicy implements DiscountPolicy {

    private static final double HIGH_VALUE_THRESHOLD = 5_000;
    private static final double HIGH_VALUE_RATE = 0.15;
    private static final double STANDARD_RATE = 0.10;

    @Override
    public double discountFor(double subtotal, int lineItemCount) {
        double rate = subtotal > HIGH_VALUE_THRESHOLD ? HIGH_VALUE_RATE : STANDARD_RATE;
        return subtotal * rate;
    }
}
