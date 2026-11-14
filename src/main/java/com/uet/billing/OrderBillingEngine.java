package com.uet.billing;

import java.util.Map;

/**
 * Backward-compatible entry point.
 *
 * This used to be the 160-line "legacy billing engine". All of its logic now lives in small,
 * focused classes under {@code com.uet.billing}; this class only keeps the old public API
 * (processOrder / setLogFilePath / getLogFilePath) so existing callers and the safety tests
 * keep working unchanged.
 */
public class OrderBillingEngine {

    private String logFilePath = "order_audit.log";
    private OrderService orderService = BillingFactory.createDefault(logFilePath);

    public boolean processOrder(Map<String, Object> orderData) {
        return orderService.processOrder(orderData);
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        this.orderService = BillingFactory.createDefault(logFilePath);
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}
