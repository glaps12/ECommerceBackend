package com.glaps12.ecommerce.dao;

import com.glaps12.ecommerce.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemEntityRepository extends JpaRepository<CartItemEntity, Long> {
}
