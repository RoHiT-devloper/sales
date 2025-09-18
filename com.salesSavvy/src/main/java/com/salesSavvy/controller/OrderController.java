package com.salesSavvy.controller;

import com.salesSavvy.entity.Order;
import com.salesSavvy.entity.Users;
import com.salesSavvy.service.OrderService;
import com.salesSavvy.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UsersService usersService;
    
    @PostMapping("/createFromCart")
    public ResponseEntity<?> createOrderFromCart(@RequestParam String username, @RequestParam Double totalAmount) {
        try {
            Users user = usersService.getUser(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            Order order = orderService.createOrderFromCart(user, totalAmount);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(@RequestParam String username) {
        try {
            Users user = usersService.getUser(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            List<Order> orders = orderService.getOrdersByUser(user);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}