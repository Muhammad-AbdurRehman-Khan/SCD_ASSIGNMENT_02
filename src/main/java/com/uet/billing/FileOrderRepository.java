package com.uet.billing;

import java.io.FileWriter;
import java.io.IOException;

/** Appends one text line per order to a log file. */
public class FileOrderRepository implements OrderRepository {

    private final String logFilePath;

    public FileOrderRepository(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    @Override
    public void save(OrderRecord record) throws IOException {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(record.getCreatedAt() + " | Order for: " + record.getCustomerEmail()
                    + " | Payable: PKR " + record.getPayableAmount()
                    + " | Status: " + record.getPaymentStatus() + "\n");
        }
    }
}
