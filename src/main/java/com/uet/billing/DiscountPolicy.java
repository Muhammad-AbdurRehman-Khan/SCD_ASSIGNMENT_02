package com.uet.billing;

/** One discount rule. Each customer type has its own implementation. */
public interface DiscountPolicy {

    /**
     * @param subtotal      sum of price * quantity of all items, before any discount
     * @param lineItemCount number of different lines in the cart
     * @return the amount (not the percentage) to subtract from the subtotal
     */
    double discountFor(double subtotal, int lineItemCount);
}
