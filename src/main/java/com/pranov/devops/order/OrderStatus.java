package com.pranov.devops.order;

import java.util.Set;

/** Kitchen workflow: PLACED -> PREPARING -> READY -> SERVED, cancellable until it is ready. */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    CANCELLED;

    public Set<OrderStatus> nextAllowed() {
        return switch (this) {
            case PLACED -> Set.of(PREPARING, CANCELLED);
            case PREPARING -> Set.of(READY, CANCELLED);
            case READY -> Set.of(SERVED);
            case SERVED, CANCELLED -> Set.of();
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return nextAllowed().contains(target);
    }
}
