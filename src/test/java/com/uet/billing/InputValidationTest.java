package com.uet.billing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Task 1 / Step 3: edge cases - empty cart, invalid e-mail, negative prices, broken items. */
class InputValidationTest extends BillingTestBase {

    private void assertRejectedWith(Map<String, Object> order, String expectedMessage) {
        assertFalse(engine.processOrder(order));
        assertTrue(console().contains(expectedMessage), "console was: " + console());
        assertFalse(logExists(), "an invalid order must not be logged");
    }

    // ---------------- whole order ----------------

    @Test
    void nullOrderIsRejected() {
        assertRejectedWith(null, "Error: Order data is empty!");
    }

    @Test
    void emptyOrderMapIsRejected() {
        assertRejectedWith(new HashMap<>(), "Error: Order data is empty!");
    }

    // ---------------- e-mail ----------------

    @Test
    void missingEmailIsRejected() {
        Map<String, Object> order = validOrder();
        order.remove("customerEmail");
        assertRejectedWith(order, "Error: Invalid customer email!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ali.uet.edu.pk", "plainstring"})
    void emailWithoutAtSignIsRejected(String email) {
        Map<String, Object> order = validOrder();
        order.put("customerEmail", email);
        assertRejectedWith(order, "Error: Invalid customer email!");
    }

    // ---------------- cart ----------------

    @Test
    void missingItemsListIsRejected() {
        Map<String, Object> order = validOrder();
        order.remove("items");
        assertRejectedWith(order, "Error: Cart has no items!");
    }

    @Test
    void emptyCartIsRejected() {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<Map<String, Object>>());
        assertRejectedWith(order, "Error: Cart has no items!");
    }

    // ---------------- individual items ----------------

    @Test
    void itemWithoutPriceIsRejected() {
        Map<String, Object> badItem = new HashMap<>();
        badItem.put("quantity", 1);
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(badItem)));
        assertRejectedWith(order, "Error: Missing item price or quantity!");
    }

    @Test
    void itemWithoutQuantityIsRejected() {
        Map<String, Object> badItem = new HashMap<>();
        badItem.put("price", 100.0);
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(badItem)));
        assertRejectedWith(order, "Error: Missing item price or quantity!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-100.0", "0.0", "-0.01"})
    void zeroOrNegativePriceIsRejected(String price) {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(item(Double.parseDouble(price), 1))));
        assertRejectedWith(order, "Error: Invalid price or quantity for item!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-10"})
    void zeroOrNegativeQuantityIsRejected(String quantity) {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(item(100.0, Integer.parseInt(quantity)))));
        assertRejectedWith(order, "Error: Invalid price or quantity for item!");
    }

    @Test
    void oneBadItemAmongGoodOnesRejectsTheWholeOrder() {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<>(List.of(item(100.0, 1), item(-5.0, 1), item(200.0, 2))));
        assertRejectedWith(order, "Error: Invalid price or quantity for item!");
    }

    // ---------------- order of the checks ----------------

    @Test
    void emailIsValidatedBeforeTheCart() {
        Map<String, Object> order = validOrder();
        order.put("customerEmail", "not-an-email");
        order.put("items", new ArrayList<Map<String, Object>>());
        assertRejectedWith(order, "Error: Invalid customer email!");
    }

    @Test
    void validationHappensBeforeAnyPaymentOrNotification() {
        Map<String, Object> order = validOrder();
        order.put("items", new ArrayList<Map<String, Object>>());

        assertFalse(engine.processOrder(order));
        assertFalse(console().contains("Processing credit card"));
        assertFalse(console().contains("DISPATCHED"));
    }
}
