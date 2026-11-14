package com.uet.billing;

import java.util.EnumMap;
import java.util.Map;

/**
 * Finds the right {@link DiscountPolicy} for a customer type and applies it.
 * A new customer type needs a new policy class and one line in the constructor -
 * no if/else ladder to edit.
 */
public class DiscountCalculator {

    private final Map<CustomerType, DiscountPolicy> policies = new EnumMap<>(CustomerType.class);

    public DiscountCalculator() {
        policies.put(CustomerType.REGULAR, new RegularDiscountPolicy());
        policies.put(CustomerType.VIP, new VipDiscountPolicy());
        policies.put(CustomerType.STUDENT, new StudentDiscountPolicy());
        policies.put(CustomerType.CORPORATE, new CorporateDiscountPolicy());
    }

    /** @return the discount amount to subtract from {@code subtotal} */
    public double calculate(CustomerType customerType, double subtotal, int lineItemCount) {
        return policies.get(customerType).discountFor(subtotal, lineItemCount);
    }
}
