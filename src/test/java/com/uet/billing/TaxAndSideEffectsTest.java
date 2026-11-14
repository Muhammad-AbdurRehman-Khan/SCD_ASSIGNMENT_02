package com.uet.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Task 1 / Step 3: tax per country, audit-log file output, e-mail / SMS notifications. */
class TaxAndSideEffectsTest extends BillingTestBase {

    // ---------------- tax ----------------

    @ParameterizedTest
    @CsvSource({
        "PK, 0.17",
        "US, 0.08",
        "UK, 0.20",
        "DE, 0.15",     // any other country -> default 15 %
        "pk, 0.15"      // country codes are case-sensitive -> lowercase falls back to default
    })
    void taxRateDependsOnCountry(String country, double expectedRate) {
        Map<String, Object> order = orderWorth("REGULAR", 2000.0);
        order.put("country", country);

        assertTrue(engine.processOrder(order));
        assertEquals(expectedPayable(2000.0, 0.0, expectedRate), loggedPayable(), DELTA);
    }

    @Test
    void missingCountryDefaultsToPakistan() {
        assertTrue(engine.processOrder(orderWorth("REGULAR", 2000.0)));
        assertEquals(2000.0 * 1.17, loggedPayable(), DELTA);
    }

    @Test
    void taxIsCalculatedAfterTheDiscount() {
        // VIP 10 % off 1000 = 900; US tax 8 % of 900 = 72 -> 972
        Map<String, Object> order = orderWorth("VIP", 1000.0);
        order.put("country", "US");

        assertTrue(engine.processOrder(order));
        assertEquals(972.0, loggedPayable(), DELTA);
    }

    // ---------------- audit log ----------------

    @Test
    void successfulOrderIsAppendedToTheAuditLog() {
        assertTrue(engine.processOrder(validOrder()));

        String log = logContents();
        assertTrue(log.contains("Order for: " + EMAIL));
        assertTrue(log.contains("Payable: PKR "));
        assertTrue(log.contains("Status: PAID"));
        assertTrue(log.endsWith("\n"));
    }

    @Test
    void everyOrderIsAppendedAsItsOwnLine() {
        assertTrue(engine.processOrder(validOrder()));
        assertTrue(engine.processOrder(validOrder()));

        assertEquals(2, logContents().split("\n").length);
    }

    @Test
    void logPathCanBeChangedAndReadBack() {
        assertEquals(logFile.toString(), engine.getLogFilePath());
        assertEquals("order_audit.log", new OrderBillingEngine().getLogFilePath());
    }

    @Test
    void orderStillSucceedsWhenTheLogFileCannotBeWritten() {
        // a directory cannot be opened as a file -> IOException inside the engine
        engine.setLogFilePath(tempDir.toString());

        assertTrue(engine.processOrder(validOrder()));
        assertTrue(console().contains("Failed to write order log:"));
        assertTrue(console().contains("[EMAIL DISPATCHED]"), "notification is still sent");
    }

    // ---------------- notifications ----------------

    @Test
    void emailIsTheDefaultNotificationChannel() {
        assertTrue(engine.processOrder(validOrder()));   // 2000 * 1.17 = 2340.00

        String expected = "[EMAIL DISPATCHED] To: " + EMAIL + " | Your order of PKR "
                + String.format("%.2f", 2340.0) + " is confirmed!";
        assertTrue(console().contains(expected), "console was: " + console());
        assertFalse(console().contains("[SMS DISPATCHED]"));
    }

    @Test
    void smsNotificationUsesTheGivenPhoneNumber() {
        Map<String, Object> order = validOrder();
        order.put("notifyChannel", "SMS");
        order.put("phone", "0345-1112223");

        assertTrue(engine.processOrder(order));

        String expected = "[SMS DISPATCHED] To: 0345-1112223 | Order confirmed! Paid: PKR "
                + String.format("%.2f", 2340.0);
        assertTrue(console().contains(expected), "console was: " + console());
        assertFalse(console().contains("[EMAIL DISPATCHED]"));
    }

    @Test
    void smsNotificationFallsBackToDefaultPhoneNumber() {
        Map<String, Object> order = validOrder();
        order.put("notifyChannel", "SMS");

        assertTrue(engine.processOrder(order));
        assertTrue(console().contains("[SMS DISPATCHED] To: 0300-0000000 |"));
    }

    @Test
    void unknownNotificationChannelSendsNothingButOrderSucceeds() {
        Map<String, Object> order = validOrder();
        order.put("notifyChannel", "PIGEON");

        assertTrue(engine.processOrder(order));
        assertFalse(console().contains("DISPATCHED"));
        assertTrue(logContents().contains("Status: PAID"));
    }

    @Test
    void noNotificationIsSentWhenPaymentFails() {
        Map<String, Object> order = validOrder();
        order.put("cardNumber", "1234");

        assertFalse(engine.processOrder(order));
        assertFalse(console().contains("DISPATCHED"));
    }
}
