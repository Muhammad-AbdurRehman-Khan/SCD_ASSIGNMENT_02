package com.uet.billing;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Shared helpers for the safety-net (characterization) tests.
 *
 * Every test talks to the engine ONLY through its public API
 * (processOrder / setLogFilePath / getLogFilePath) and observes the three
 * outputs that matter: the boolean result, the console text and the audit log.
 * Because of that, the very same tests must keep passing after the refactoring.
 */
abstract class BillingTestBase {

    protected static final String EMAIL = "ali@uet.edu.pk";
    protected static final double DELTA = 0.001;

    /** JUnit creates a fresh temporary folder for every test and deletes it afterwards. */
    @TempDir
    Path tempDir;

    protected OrderBillingEngine engine;
    protected Path logFile;

    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void setUpEngineAndCaptureConsole() {
        engine = new OrderBillingEngine();
        logFile = tempDir.resolve("audit.log");
        engine.setLogFilePath(logFile.toString());

        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, UTF_8));
    }

    @AfterEach
    void restoreConsole() {
        System.setOut(originalOut);
    }

    // ------------------------------------------------------------------
    // Order builders
    // ------------------------------------------------------------------

    /** A valid REGULAR / PK / CREDIT_CARD / EMAIL order worth PKR 2000 before tax. */
    protected Map<String, Object> validOrder() {
        Map<String, Object> order = new HashMap<>();
        order.put("customerEmail", EMAIL);
        order.put("items", new ArrayList<>(List.of(item(1000.0, 2))));
        order.put("paymentMethod", "CREDIT_CARD");
        order.put("cardNumber", "1234567890123456");
        return order;
    }

    protected Map<String, Object> item(double price, int quantity) {
        Map<String, Object> item = new HashMap<>();
        item.put("price", price);
        item.put("quantity", quantity);
        return item;
    }

    /** An order whose single line item is worth exactly {@code subtotal}. */
    protected Map<String, Object> orderWorth(String customerType, double subtotal) {
        Map<String, Object> order = validOrder();
        order.put("customerType", customerType);
        order.put("items", new ArrayList<>(List.of(item(subtotal, 1))));
        return order;
    }

    /** An order with {@code count} line items, each costing PKR 100 (subtotal = count * 100). */
    protected Map<String, Object> orderWithLineItems(String customerType, int count) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(item(100.0, 1));
        }
        Map<String, Object> order = validOrder();
        order.put("customerType", customerType);
        order.put("items", items);
        return order;
    }

    // ------------------------------------------------------------------
    // Observation helpers
    // ------------------------------------------------------------------

    protected String console() {
        return captured.toString(UTF_8);
    }

    protected boolean logExists() {
        return Files.exists(logFile);
    }

    protected String logContents() {
        try {
            return Files.readString(logFile, UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Audit log could not be read: " + logFile, e);
        }
    }

    /** Reads the "Payable: PKR xxx" figure that the engine wrote to the audit log. */
    protected double loggedPayable() {
        Matcher matcher = Pattern.compile("Payable: PKR (\\S+) \\|").matcher(logContents());
        if (!matcher.find()) {
            throw new IllegalStateException("No payable amount in log: " + logContents());
        }
        return Double.parseDouble(matcher.group(1));
    }

    /** payable = (subtotal - discount) + (subtotal - discount) * taxRate, same formula as the legacy code. */
    protected double expectedPayable(double subtotal, double discount, double taxRate) {
        double net = subtotal - discount;
        return net + net * taxRate;
    }
}
