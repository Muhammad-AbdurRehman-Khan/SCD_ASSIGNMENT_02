package com.uet.billing;

import java.io.IOException;

/**
 * Where processed orders are stored. The business code only knows this interface,
 * so the file could be replaced by a database without touching the pricing rules.
 */
public interface OrderRepository {

    void save(OrderRecord record) throws IOException;
}
