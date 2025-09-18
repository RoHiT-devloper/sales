package com.salesSavvy.repository;

import com.salesSavvy.entity.Order;
import com.salesSavvy.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(Users user);
    List<Order> findByUser(Users user);
}