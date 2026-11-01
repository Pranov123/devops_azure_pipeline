package com.pranov.devops.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderLineRequest(
            @NotNull Long menuItemId,
            @Min(1) @Max(20) int quantity) {
    }

    public record PlaceOrderRequest(
            @NotBlank @Size(max = 80) String customerName,
            @NotNull OrderType orderType,
            @Min(1) @Max(20) Integer tableNumber,
            @NotEmpty @Size(max = 30) List<@Valid OrderLineRequest> items) {
    }

    public record StatusUpdateRequest(@NotNull OrderStatus status) {
    }

    public record OrderLineResponse(Long menuItemId, String name, BigDecimal unitPrice, int quantity,
                                    BigDecimal lineTotal) {
    }

    public record OrderResponse(Long id, String customerName, OrderType orderType, Integer tableNumber,
                                OrderStatus status, Set<OrderStatus> nextStatuses,
                                List<OrderLineResponse> items, BigDecimal subtotal, BigDecimal tax,
                                BigDecimal total, LocalDateTime createdAt, LocalDateTime updatedAt) {

        static OrderResponse from(CustomerOrder order) {
            List<OrderLineResponse> lines = order.getItems().stream()
                    .map(i -> new OrderLineResponse(i.getMenuItemId(), i.getItemName(), i.getUnitPrice(),
                            i.getQuantity(), i.getLineTotal()))
                    .toList();
            return new OrderResponse(order.getId(), order.getCustomerName(), order.getOrderType(),
                    order.getTableNumber(), order.getStatus(), order.getStatus().nextAllowed(), lines,
                    order.getSubtotal(), order.getTax(), order.getTotal(), order.getCreatedAt(),
                    order.getUpdatedAt());
        }
    }
}
