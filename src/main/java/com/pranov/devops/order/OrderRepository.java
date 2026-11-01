package com.pranov.devops.order;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}
