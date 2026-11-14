package com.uet.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Task 1 / Step 3: payment options - Credit Card, EasyPaisa, Cash on Delivery, unsupported. */
class PaymentMethodTest extends BillingTestBase {

    // ---------------- CREDIT_CARD ----------------

    @Test
    void validCreditCardIsPaid() {
        assertTrue(engine.processOrder(validOrder()));

        assertTrue(console().contains("Processing credit card payment of PKR "));
        assertTrue(logContents().contains("Status: PAID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789012345", "12345678901234567", "1234abcd12345678", "1234 5678 1234 56"})
    void creditCardWithWrongLengthOrNonDigitsIsDeclined(String cardNumber) {
        Map<String, Object> order = validOrder();
        order.put("cardNumber", cardNumber);

        assertFalse(engine.processOrder(order));
        assertTrue(console().contains("Card declined: Must be 16 digits."));
        assertFalse(logExists(), "a declined payment must not be logged");
    }

    @Test
    void creditCardWithoutCardNumberIsDeclined() {
        Map<String, Object> order = validOrder();
        order.remove("cardNumber");

        assertFalse(engine.processOrder(order));
        assertTrue(console().contains("Card declined: Must be 16 digits."));
    }

    // ---------------- EASYPAISA ----------------

    private Map<String, Object> easyPaisaOrder(String mobileNumber) {
        Map<String, Object> order = validOrder();
        order.put("paymentMethod", "EASYPAISA");
        order.remove("cardNumber");
        if (mobileNumber != null) {
            order.put("mobileNumber", mobileNumber);
        }
        return order;
    }

    @Test
    void validEasyPaisaNumberIsPaid() {
        assertTrue(engine.processOrder(easyPaisaOrder("03001234567")));

        assertTrue(console().contains("Sending EasyPaisa OTP push to 03001234567 for PKR "));
        assertTrue(logContents().contains("Status: PAID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0300123456", "030012345678", "04001234567", "92300123456"})
    void easyPaisaWithInvalidMobileNumberFails(String mobileNumber) {
        assertFalse(engine.processOrder(easyPaisaOrder(mobileNumber)));
        assertTrue(console().contains("EasyPaisa failed: Invalid mobile number."));
        assertFalse(logExists());
    }

    @Test
    void easyPaisaWithoutMobileNumberFails() {
        assertFalse(engine.processOrder(easyPaisaOrder(null)));
        assertTrue(console().contains("EasyPaisa failed: Invalid mobile number."));
    }

    // ---------------- CASH_ON_DELIVERY ----------------

    private Map<String, Object> cashOnDeliveryOrder(double subtotal) {
        Map<String, Object> order = orderWorth("REGULAR", subtotal);
        order.put("paymentMethod", "CASH_ON_DELIVERY");
        order.remove("cardNumber");
        return order;
    }

    @Test
    void cashOnDeliveryUnderLimitIsPendingCash() {
        // 20000 - 500 flat discount = 19500; + 17 % tax = 22815 (< 25000)
        assertTrue(engine.processOrder(cashOnDeliveryOrder(20000.0)));

        assertTrue(logContents().contains("Status: PENDING_CASH"));
    }

    @Test
    void cashOnDeliveryAboveLimitIsRejected() {
        // 22000 - 500 = 21500; + 17 % tax = 25155 (> 25000)
        assertFalse(engine.processOrder(cashOnDeliveryOrder(22000.0)));

        assertTrue(console().contains("Cash on delivery not allowed for orders above PKR 25,000."));
        assertFalse(logExists());
    }

    @Test
    void cashOnDeliveryLimitIsCheckedAgainstPayableAmountAfterTax() {
        // UK tax is 20 %. Flat discount 500 applies to both orders.
        // 21000 -> (21000 - 500) * 1.2 = 24600 (<= 25000) -> accepted
        Map<String, Object> allowed = cashOnDeliveryOrder(21000.0);
        allowed.put("country", "UK");
        assertTrue(engine.processOrder(allowed));

        // 21500 -> (21500 - 500) * 1.2 = 25200 (> 25000) -> rejected, although the subtotal itself is below 25000
        Map<String, Object> rejected = cashOnDeliveryOrder(21500.0);
        rejected.put("country", "UK");
        assertFalse(engine.processOrder(rejected));
        assertTrue(console().contains("Cash on delivery not allowed"));
    }

    @Test
    void cashOnDeliveryAtExactlyTheLimitIsAccepted() {
        // UK tax 20 %, flat discount 500: (21333.333333333332 - 500) * 1.2 is exactly 25000.0.
        // The rule is "payable > 25000", so the limit itself is still allowed.
        Map<String, Object> order = cashOnDeliveryOrder(21333.333333333332);
        order.put("country", "UK");

        assertTrue(engine.processOrder(order));
        assertEquals(25000.0, loggedPayable(), 0.0);
        assertTrue(logContents().contains("Status: PENDING_CASH"));
    }

    @Test
    void cashOnDeliveryPrintsNoPaymentMessageWhenAccepted() {
        assertTrue(engine.processOrder(cashOnDeliveryOrder(2000.0)));

        assertFalse(console().contains("Processing"));
        assertFalse(console().contains("Error"));
    }

    // ---------------- Unsupported / missing method ----------------

    @Test
    void unsupportedPaymentMethodIsRejected() {
        Map<String, Object> order = validOrder();
        order.put("paymentMethod", "BITCOIN");

        assertFalse(engine.processOrder(order));
        assertTrue(console().contains("Error: Unsupported payment method: BITCOIN"));
        assertFalse(logExists());
    }

    @Test
    void missingPaymentMethodDefaultsToCashWhichIsUnsupported() {
        Map<String, Object> order = validOrder();
        order.remove("paymentMethod");

        assertFalse(engine.processOrder(order));
        assertTrue(console().contains("Error: Unsupported payment method: CASH"));
    }
}
