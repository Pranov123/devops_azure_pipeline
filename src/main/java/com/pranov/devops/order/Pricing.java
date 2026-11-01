package com.pranov.devops.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Bill calculation: subtotal + 5% GST (restaurant rate), rounded to paise. */
public final class Pricing {

    public static final BigDecimal GST_RATE = new BigDecimal("0.05");

    private Pricing() {
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal tax(BigDecimal subtotal) {
        return subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal total(BigDecimal subtotal) {
        return subtotal.add(tax(subtotal)).setScale(2, RoundingMode.HALF_UP);
    }
}
