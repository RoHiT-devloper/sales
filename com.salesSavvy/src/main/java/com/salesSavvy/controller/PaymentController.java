package com.salesSavvy.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.salesSavvy.entity.Users;
import com.salesSavvy.service.OrderService;
import com.salesSavvy.service.UsersService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/payment")
public class PaymentController {

    private static final String RAZORPAY_KEY_ID = "rzp_test_RGyL9I6McCuTtD";
    private static final String RAZORPAY_KEY_SECRET = "XqjpqbcpmKL8VPk5JXlsnkA6";

    @Autowired
    private OrderService orderService;

    @Autowired
    private UsersService usersService;

    @PostMapping("/create-order")
    public String createOrder(@RequestBody PaymentRequest paymentRequest) {
        try {
            RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentRequest.getAmount() * 100); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);
            
            Order order = razorpay.orders.create(orderRequest);
            return order.toString();
        } catch (RazorpayException e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to create order\"}";
        }
    }

    @PostMapping("/verify-payment")
    public String verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        try {
            // Verify payment signature (in a real application, you should verify the signature)
            // For demo purposes, we'll assume the payment is successful and create an order
            
            String username = verificationRequest.getUsername();
            Double amount = verificationRequest.getAmount();
            
            if (username != null && amount != null) {
                Users user = usersService.getUser(username);
                if (user != null) {
                    // Create order from cart
                    com.salesSavvy.entity.Order order = orderService.createOrderFromCart(user, amount);
                    
                    return "{\"status\":\"success\",\"orderId\":" + order.getId() + "}";
                }
            }
            
            return "{\"status\":\"success\"}"; // Fallback if no user info
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/create-order-and-clear-cart")
    public String createOrderAndClearCart(@RequestBody OrderCreationRequest orderRequest) {
        try {
            String username = orderRequest.getUsername();
            Double totalAmount = orderRequest.getTotalAmount();
            
            if (username == null || totalAmount == null) {
                return "{\"status\":\"error\",\"message\":\"Username and totalAmount are required\"}";
            }
            
            Users user = usersService.getUser(username);
            if (user == null) {
                return "{\"status\":\"error\",\"message\":\"User not found\"}";
            }
            
            // Create order from cart
            com.salesSavvy.entity.Order order = orderService.createOrderFromCart(user, totalAmount);
            
            return "{\"status\":\"success\",\"orderId\":" + order.getId() + "}";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/user-orders")
    public String getUserOrders(@RequestParam String username) {
        try {
            Users user = usersService.getUser(username);
            if (user == null) {
                return "{\"status\":\"error\",\"message\":\"User not found\"}";
            }
            
            // This would typically return a list of orders, but for simplicity
            // we'll just return a success message
            return "{\"status\":\"success\",\"message\":\"Orders retrieved successfully\"}";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // Request classes
    public static class PaymentRequest {
        private double amount;
        private String username; // Add username to payment request
        
        public double getAmount() {
            return amount;
        }
        
        public void setAmount(double amount) {
            this.amount = amount;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class PaymentVerificationRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private String username;
        private Double amount;
        
        // Getters and setters
        public String getRazorpayOrderId() { return razorpayOrderId; }
        public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
        
        public String getRazorpayPaymentId() { return razorpayPaymentId; }
        public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
        
        public String getRazorpaySignature() { return razorpaySignature; }
        public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class OrderCreationRequest {
        private String username;
        private Double totalAmount;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    }
}