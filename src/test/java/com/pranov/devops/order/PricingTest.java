package com.pranov.devops.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PricingTest {

    @Test
    void lineTotalMultipliesPriceByQuantity() {
        assertEquals(new BigDecimal("747.00"), Pricing.lineTotal(new BigDecimal("249.00"), 3));
    }

    @Test
    void gstIsFivePercentRoundedToPaise() {
        assertEquals(new BigDecimal("33.75"), Pricing.tax(new BigDecimal("675.00")));
        // 5% of 59.90 = 2.995 -> rounds half up to 3.00
        assertEquals(new BigDecimal("3.00"), Pricing.tax(new BigDecimal("59.90")));
    }

    @Test
    void totalIsSubtotalPlusGst() {
        assertEquals(new BigDecimal("708.75"), Pricing.total(new BigDecimal("675.00")));
        assertEquals(new BigDecimal("0.00"), Pricing.total(BigDecimal.ZERO));
    }
}
