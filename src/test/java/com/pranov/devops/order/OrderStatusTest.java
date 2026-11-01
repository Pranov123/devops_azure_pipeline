package com.pranov.devops.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void followsKitchenWorkflow() {
        assertTrue(OrderStatus.PLACED.canMoveTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.PREPARING.canMoveTo(OrderStatus.READY));
        assertTrue(OrderStatus.READY.canMoveTo(OrderStatus.SERVED));
    }

    @Test
    void cannotSkipSteps() {
        assertFalse(OrderStatus.PLACED.canMoveTo(OrderStatus.READY));
        assertFalse(OrderStatus.PLACED.canMoveTo(OrderStatus.SERVED));
        assertFalse(OrderStatus.PREPARING.canMoveTo(OrderStatus.PLACED));
    }

    @Test
    void canCancelOnlyBeforeReady() {
        assertTrue(OrderStatus.PLACED.canMoveTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.PREPARING.canMoveTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.READY.canMoveTo(OrderStatus.CANCELLED));
    }

    @Test
    void finishedOrdersAreFinal() {
        assertTrue(OrderStatus.SERVED.nextAllowed().isEmpty());
        assertTrue(OrderStatus.CANCELLED.nextAllowed().isEmpty());
    }
}
