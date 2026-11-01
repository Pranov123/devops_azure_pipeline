package com.pranov.devops.order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pranov.devops.common.BusinessRuleException;
import com.pranov.devops.common.NotFoundException;
import com.pranov.devops.menu.MenuItem;
import com.pranov.devops.menu.MenuItemRepository;
import com.pranov.devops.order.OrderDtos.OrderLineRequest;
import com.pranov.devops.order.OrderDtos.OrderResponse;
import com.pranov.devops.order.OrderDtos.PlaceOrderRequest;

@Service
@Transactional(readOnly = true)
public class OrderService {

    /** Orders the kitchen still has to act on. */
    static final EnumSet<OrderStatus> ACTIVE = EnumSet.of(OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.READY);

    private final OrderRepository orders;
    private final MenuItemRepository menu;
    private final Clock clock;

    public OrderService(OrderRepository orders, MenuItemRepository menu, Clock clock) {
        this.orders = orders;
        this.menu = menu;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse place(PlaceOrderRequest request) {
        if (request.orderType() == OrderType.DINE_IN && request.tableNumber() == null) {
            throw new BusinessRuleException("Table number is required for dine-in orders");
        }
        Integer table = request.orderType() == OrderType.DINE_IN ? request.tableNumber() : null;
        CustomerOrder order = new CustomerOrder(request.customerName().trim(), request.orderType(), table, now());

        // The same dish added twice becomes one line with the combined quantity
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderLineRequest line : request.items()) {
            quantities.merge(line.menuItemId(), line.quantity(), Integer::sum);
        }
        quantities.forEach((menuItemId, quantity) -> {
            MenuItem item = menu.findById(menuItemId)
                    .orElseThrow(() -> new BusinessRuleException("Menu item " + menuItemId + " does not exist"));
            if (!item.isAvailable()) {
                throw new BusinessRuleException(item.getName() + " is not available right now");
            }
            order.addItem(new OrderItem(item, quantity));
        });

        return OrderResponse.from(orders.save(order));
    }

    public OrderResponse get(Long id) {
        return OrderResponse.from(find(id));
    }

    /** Active orders by default (kitchen view); all orders with {@code includeCompleted}. */
    public List<OrderResponse> list(boolean includeCompleted) {
        EnumSet<OrderStatus> statuses = includeCompleted ? EnumSet.allOf(OrderStatus.class) : ACTIVE;
        return orders.findByStatusInOrderByCreatedAtAsc(statuses).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus target) {
        CustomerOrder order = find(id);
        if (!order.getStatus().canMoveTo(target)) {
            throw new BusinessRuleException("Cannot change order " + id + " from " + order.getStatus() + " to " + target);
        }
        order.moveTo(target, now());
        return OrderResponse.from(order);
    }

    private CustomerOrder find(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
