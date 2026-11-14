package com.uet.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proof of the Open/Closed Principle (the "3-minute live challenge").
 *
 * A brand-new payment method (JazzCash) and a brand-new notification channel (WhatsApp)
 * are added below by writing two NEW classes only. Not a single line of the existing
 * business classes (OrderService, parser, calculators, other strategies) is changed.
 */
class OpenClosedExtensibilityTest {

    @TempDir
    Path tempDir;

    // ---- the two new classes -------------------------------------------------

    /** JazzCash: same 11-digit "03..." mobile number rule as other wallets. */
    static class JazzCashPayment implements PaymentStrategy {
        @Override
        public String methodName() {
            return "JAZZCASH";
        }

        @Override
        public PaymentResult pay(PaymentRequest request) {
            String mobile = request.getMobileNumber();
            if (mobile.length() == 11 && mobile.startsWith("03")) {
                return PaymentResult.paid("Charging JazzCash wallet " + mobile + " PKR " + request.getAmount());
            }
            return PaymentResult.declined("JazzCash failed: Invalid mobile number.");
        }
    }

    /** WhatsApp: remembers messages instead of sending them, so the test can inspect them. */
    static class WhatsAppNotification implements NotificationService {
        final List<String> sent = new ArrayList<>();

        @Override
        public String channel() {
            return "WHATSAPP";
        }

        @Override
        public void send(NotificationRequest request) {
            sent.add(request.getPhone() + ": order confirmed, PKR " + String.format("%.2f", request.getAmount()));
        }
    }

    // ---- wiring (only the lists are extended) -------------------------------

    private final WhatsAppNotification whatsApp = new WhatsAppNotification();

    private OrderService serviceWithNewFeatures(Path logFile) {
        List<PaymentStrategy> payments = new ArrayList<>(BillingFactory.defaultPaymentStrategies());
        payments.add(new JazzCashPayment());

        List<NotificationService> notifications = new ArrayList<>(BillingFactory.defaultNotificationServices());
        notifications.add(whatsApp);

        return new OrderService(new OrderRequestParser(), new DiscountCalculator(), new TaxCalculator(),
                payments, new FileOrderRepository(logFile.toString()), notifications);
    }

    private Map<String, Object> order(String paymentMethod, String mobile, String channel) {
        Map<String, Object> item = new HashMap<>();
        item.put("price", 1000.0);
        item.put("quantity", 2);

        Map<String, Object> order = new HashMap<>();
        order.put("customerEmail", "ali@uet.edu.pk");
        order.put("items", new ArrayList<>(List.of(item)));
        order.put("paymentMethod", paymentMethod);
        order.put("mobileNumber", mobile);
        order.put("notifyChannel", channel);
        order.put("phone", "0345-1112223");
        return order;
    }

    // ---- tests ---------------------------------------------------------------

    @Test
    void newPaymentMethodAndNotificationWorkWithoutChangingExistingClasses() throws IOException {
        Path logFile = tempDir.resolve("audit.log");

        boolean accepted = serviceWithNewFeatures(logFile)
                .processOrder(order("JAZZCASH", "03001234567", "WHATSAPP"));

        assertTrue(accepted);
        assertTrue(Files.readString(logFile).contains("Status: PAID"));
        assertEquals(1, whatsApp.sent.size());
        assertEquals("0345-1112223: order confirmed, PKR 2340.00", whatsApp.sent.get(0));
    }

    @Test
    void newPaymentMethodKeepsItsOwnValidationRules() {
        Path logFile = tempDir.resolve("audit.log");

        boolean accepted = serviceWithNewFeatures(logFile)
                .processOrder(order("JAZZCASH", "12345", "WHATSAPP"));

        assertFalse(accepted);
        assertFalse(Files.exists(logFile));
        assertTrue(whatsApp.sent.isEmpty());
    }

    @Test
    void defaultConfigurationStillRejectsMethodsThatWereNotRegistered() {
        Path logFile = tempDir.resolve("audit.log");
        OrderService defaultService = BillingFactory.createDefault(logFile.toString());

        assertFalse(defaultService.processOrder(order("JAZZCASH", "03001234567", "EMAIL")));
    }
}
