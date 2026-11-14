package com.uet.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Task 1 / Step 2: characterization tests for the discount rules.
 * All orders ship to PK (default) so the tax rate is a constant 17 %.
 */
class DiscountCharacterizationTest extends BillingTestBase {

    private static final double PK_TAX = 0.17;

    // ---------------- REGULAR customers ----------------

    @ParameterizedTest
    @CsvSource({
        "REGULAR, 2000.0,   0.0",     // normal order, no discount
        "REGULAR, 10000.0,  0.0",     // boundary: rule is "> 10000", so exactly 10000 gets nothing
        "REGULAR, 10000.01, 500.0",   // just above the boundary -> flat PKR 500
        "REGULAR, 12000.0,  500.0"    // large regular order -> flat PKR 500
    })
    void regularCustomerDiscountRules(String type, double subtotal, double expectedDiscount) {
        assertTrue(engine.processOrder(orderWorth(type, subtotal)));
        assertEquals(expectedPayable(subtotal, expectedDiscount, PK_TAX), loggedPayable(), DELTA);
    }

    @Test
    void missingCustomerTypeDefaultsToRegular() {
        Map<String, Object> order = orderWorth("REGULAR", 12000.0);
        order.remove("customerType");

        assertTrue(engine.processOrder(order));
        assertEquals(expectedPayable(12000.0, 500.0, PK_TAX), loggedPayable(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({
        "GOLD, 12000.0, 500.0",   // unknown type -> treated like REGULAR
        "vip,  8000.0,  0.0"      // type names are case-sensitive -> lowercase "vip" is NOT a VIP
    })
    void unknownCustomerTypeFallsBackToRegularRules(String type, double subtotal, double expectedDiscount) {
        assertTrue(engine.processOrder(orderWorth(type, subtotal)));
        assertEquals(expectedPayable(subtotal, expectedDiscount, PK_TAX), loggedPayable(), DELTA);
    }

    // ---------------- VIP customers ----------------

    @ParameterizedTest
    @CsvSource({
        "VIP, 1000.0,  100.0",      // 10 % for small orders
        "VIP, 5000.0,  500.0",      // boundary: rule is "> 5000", so exactly 5000 still gets 10 %
        "VIP, 5001.0,  750.15",     // just above the boundary -> 15 %
        "VIP, 8000.0,  1200.0"      // 15 % for large orders
    })
    void vipCustomerDiscountRules(String type, double subtotal, double expectedDiscount) {
        assertTrue(engine.processOrder(orderWorth(type, subtotal)));
        assertEquals(expectedPayable(subtotal, expectedDiscount, PK_TAX), loggedPayable(), DELTA);
    }

    // ---------------- STUDENT customers ----------------

    @ParameterizedTest
    @CsvSource({
        "STUDENT, 1000.0,  80.0",     // flat 8 %
        "STUDENT, 20000.0, 1600.0"    // still 8 %, no extra regular-order bonus
    })
    void studentCustomerGetsEightPercent(String type, double subtotal, double expectedDiscount) {
        assertTrue(engine.processOrder(orderWorth(type, subtotal)));
        assertEquals(expectedPayable(subtotal, expectedDiscount, PK_TAX), loggedPayable(), DELTA);
    }

    // ---------------- CORPORATE customers ----------------

    @Test
    void corporateWithFewerThanTenLineItemsGetsFivePercent() {
        // 9 line items * PKR 100 = 900 -> 5 %
        assertTrue(engine.processOrder(orderWithLineItems("CORPORATE", 9)));
        assertEquals(expectedPayable(900.0, 45.0, PK_TAX), loggedPayable(), DELTA);
    }

    @Test
    void corporateWithTenOrMoreLineItemsGetsTwentyPercent() {
        // boundary: rule is ">= 10" line items -> 10 * PKR 100 = 1000 -> 20 %
        assertTrue(engine.processOrder(orderWithLineItems("CORPORATE", 10)));
        assertEquals(expectedPayable(1000.0, 200.0, PK_TAX), loggedPayable(), DELTA);
    }

    @Test
    void corporateBulkRuleCountsLineItemsNotQuantity() {
        // ONE line item with quantity 50 is still "1 item" -> 5 %, not 20 %
        Map<String, Object> order = validOrder();
        order.put("customerType", "CORPORATE");
        order.put("items", new ArrayList<>(List.of(item(100.0, 50))));

        assertTrue(engine.processOrder(order));
        assertEquals(expectedPayable(5000.0, 250.0, PK_TAX), loggedPayable(), DELTA);
    }

    // ---------------- Subtotal calculation ----------------

    @Test
    void subtotalMultipliesPriceByQuantityAndSumsAllItems() {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(item(100.0, 3), item(50.0, 2))));   // 300 + 100 = 400

        assertTrue(engine.processOrder(order));
        assertEquals(expectedPayable(400.0, 0.0, PK_TAX), loggedPayable(), DELTA);
    }
}
