package com.salesSavvy.service;

import com.salesSavvy.entity.*;
import com.salesSavvy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImplementation implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UsersService usersService;
    
    @Autowired
    private CartService cartService;
    
    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
    
    @Override
    public List<Order> getOrdersByUser(Users user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    @Override
    public Order createOrderFromCart(Users user, Double totalAmount) {
        // Get user's cart
        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Create new order
        Order order = new Order(user, totalAmount, "COMPLETED");
        
        // Convert cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                cartItem.getProduct(),
                cartItem.getQuantity(),
                (double) cartItem.getProduct().getPrice()
            );
            order.addItem(orderItem);
        }
        
        // Save the order
        Order savedOrder = saveOrder(order);
        
        // Clear the cart
        cart.getItems().clear();
        usersService.saveUser(user);
        
        return savedOrder;
    }
}