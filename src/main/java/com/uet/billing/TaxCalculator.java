package com.uet.billing;

import java.util.HashMap;
import java.util.Map;

/** Tax rate per country. Countries that are not listed pay the default rate of 15 %. */
public class TaxCalculator {

    private static final double DEFAULT_RATE = 0.15;

    private final Map<String, Double> ratesByCountry = new HashMap<>();

    public TaxCalculator() {
        ratesByCountry.put("PK", 0.17);
        ratesByCountry.put("US", 0.08);
        ratesByCountry.put("UK", 0.20);
    }

    public double rateFor(String country) {
        return ratesByCountry.getOrDefault(country, DEFAULT_RATE);
    }

    /** @return the tax amount to add on top of {@code netAmount} */
    public double calculate(String country, double netAmount) {
        return netAmount * rateFor(country);
    }
}
