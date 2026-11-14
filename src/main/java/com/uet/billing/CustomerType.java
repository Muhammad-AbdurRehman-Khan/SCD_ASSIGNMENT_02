package com.uet.billing;

/**
 * Kinds of customers the shop knows about.
 * Replaces the magic strings "VIP", "STUDENT", ... of the legacy code.
 */
public enum CustomerType {
    REGULAR,
    VIP,
    STUDENT,
    CORPORATE;

    /**
     * Converts the text of an order into a type.
     * Matching is case-sensitive and unknown values count as {@link #REGULAR},
     * exactly like the legacy if/else chain behaved.
     */
    public static CustomerType from(String value) {
        for (CustomerType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return REGULAR;
    }
}
