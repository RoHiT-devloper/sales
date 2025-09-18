package com.salesSavvy.service;

import com.salesSavvy.entity.Order;
import com.salesSavvy.entity.Users;
import java.util.List;

public interface OrderService {
    Order saveOrder(Order order);
    List<Order> getOrdersByUser(Users user);
    Order createOrderFromCart(Users user, Double totalAmount);
}