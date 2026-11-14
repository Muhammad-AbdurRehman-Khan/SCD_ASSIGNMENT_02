package com.uet.billing;

/** Corporate customers: 20 % when the cart has 10 or more lines, otherwise 5 %. */
public class CorporateDiscountPolicy implements DiscountPolicy {

    private static final int BULK_LINE_ITEMS = 10;
    private static final double BULK_RATE = 0.20;
    private static final double STANDARD_RATE = 0.05;

    @Override
    public double discountFor(double subtotal, int lineItemCount) {
        double rate = lineItemCount >= BULK_LINE_ITEMS ? BULK_RATE : STANDARD_RATE;
        return subtotal * rate;
    }
}
