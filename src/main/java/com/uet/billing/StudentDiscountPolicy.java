package com.uet.billing;

/** Students: always 8 %. */
public class StudentDiscountPolicy implements DiscountPolicy {

    private static final double STUDENT_RATE = 0.08;

    @Override
    public double discountFor(double subtotal, int lineItemCount) {
        return subtotal * STUDENT_RATE;
    }
}
