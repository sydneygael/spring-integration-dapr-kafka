package com.example.sadjoum.repository;

import com.example.sadjoum.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}