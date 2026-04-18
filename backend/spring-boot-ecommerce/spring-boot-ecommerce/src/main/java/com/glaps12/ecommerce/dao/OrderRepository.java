package com.glaps12.ecommerce.dao;

import com.glaps12.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByDateCreatedDesc(Long userId);
    Optional<Order> findByOrderTrackingNumber(String orderTrackingNumber);
}
