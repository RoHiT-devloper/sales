import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./CartPage.css";

const CartPage = () => {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPrice, setTotalPrice] = useState(0);
  const [processingPayment, setProcessingPayment] = useState(false);
  const [razorpayLoaded, setRazorpayLoaded] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCartItems();
    loadRazorpay();
  }, []);

  // Function to load Razorpay script
  const loadRazorpay = () => {
    return new Promise((resolve) => {
      if (window.Razorpay) {
        setRazorpayLoaded(true);
        resolve(true);
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => {
        setRazorpayLoaded(true);
        resolve(true);
      };
      script.onerror = () => {
        console.error('Failed to load Razorpay script');
        resolve(false);
      };
      document.body.appendChild(script);
    });
  };

  // Add this function to create an order after payment
const createOrder = async () => {
    const username = localStorage.getItem("username");
    if (!username) return;

    try {
        const response = await fetch(`http://localhost:8080/api/orders/createFromCart?username=${username}&totalAmount=${totalPrice}`, {
            method: "POST",
        });

        if (!response.ok) {
            throw new Error("Failed to create order");
        }
        
        return await response.json();
    } catch (error) {
        console.error("Error creating order:", error);
        throw error;
    }
};

  const fetchCartItems = async () => {
    const username = localStorage.getItem("username");
    if (!username) {
      alert("Please log in to view your cart.");
      navigate("/signin");
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/cart/getCart?username=${username}`);
      if (response.ok) {
        const cartData = await response.json();
        setCartItems(cartData.items || []);
        setTotalPrice(cartData.totalPrice || 0);
      } else {
        console.error('Failed to fetch cart items');
      }
    } catch (error) {
      console.error('Error fetching cart items:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveItem = async (productId) => {
    const username = localStorage.getItem("username");
    if (!username) return;

    try {
      const response = await fetch(`http://localhost:8080/api/cart/remove?productId=${productId}&username=${username}`, {
        method: "DELETE",
      });

      if (response.ok) {
        fetchCartItems();
        alert("Item removed from cart!");
      } else {
        const errorText = await response.text();
        alert(errorText || "Failed to remove item from cart.");
      }
    } catch (error) {
      console.error('Error removing item:', error);
      alert("Something went wrong while removing the item.");
    }
  };

  const handleUpdateQuantity = async (productId, newQuantity) => {
    if (newQuantity < 1) return;
    
    const username = localStorage.getItem("username");
    if (!username) return;

    try {
      const response = await fetch("http://localhost:8080/api/cart/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productId: productId,
          username: username,
          quantity: newQuantity,
        }),
      });

      if (response.ok) {
        fetchCartItems();
      } else {
        const errorText = await response.text();
        alert(errorText || "Failed to update quantity.");
      }
    } catch (error) {
      console.error('Error updating quantity:', error);
      alert("Something went wrong while updating quantity.");
    }
  };

// Modify the initiateRazorpayPayment function
const initiateRazorpayPayment = async () => {
    const razorpayAvailable = await loadRazorpay();
    if (!razorpayAvailable) {
        alert("Payment service is temporarily unavailable. Please try again later.");
        return;
    }

    setProcessingPayment(true);
    
    try {
        // First create a Razorpay order
        const orderResponse = await fetch("http://localhost:8080/api/payment/create-order", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                amount: totalPrice,
                username: localStorage.getItem("username")
            }),
        });

        if (!orderResponse.ok) {
            throw new Error("Failed to create payment order");
        }

        const orderData = await orderResponse.json();
        const razorpayOrder = JSON.parse(orderData);

        const options = {
            key: "rzp_test_1DP5mmOlF5G5ag",
            amount: razorpayOrder.amount,
            currency: razorpayOrder.currency,
            order_id: razorpayOrder.id,
            name: "SalesSavvy Store",
            description: "Thank you for your purchase",
            image: "https://cdn.razorpay.com/logos/7K3b6d18wHwKzL_medium.png",
            handler: async function(response) {
                try {
                    // Verify payment and create order
                    const verifyResponse = await fetch("http://localhost:8080/api/payment/verify-payment", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({
                            razorpayOrderId: response.razorpay_order_id,
                            razorpayPaymentId: response.razorpay_payment_id,
                            razorpaySignature: response.razorpay_signature,
                            username: localStorage.getItem("username"),
                            amount: totalPrice
                        }),
                    });

                    const verifyResult = await verifyResponse.json();
                    
                    if (verifyResult.status === "success") {
                        alert(`Payment successful! Order #${verifyResult.orderId} has been created.`);
                        clearCartAfterPayment();
                    } else {
                        alert("Payment verification failed. Please contact support.");
                        setProcessingPayment(false);
                    }
                } catch (error) {
                    console.error('Payment verification error:', error);
                    alert("Payment was successful but order creation failed. Please contact support.");
                    setProcessingPayment(false);
                }
            },
            prefill: {
                name: localStorage.getItem("username") || "Customer",
                email: "customer@example.com",
                contact: "9000090000"
            },
            notes: {
                address: "SalesSavvy Customer"
            },
            theme: {
                color: "#4285f4"
            }
        };
        
        const razorpayInstance = new window.Razorpay(options);
        
        razorpayInstance.on('payment.failed', function(response) {
            alert(`Payment failed: ${response.error.description}`);
            setProcessingPayment(false);
        });
        
        razorpayInstance.open();
        
    } catch (error) {
        console.error('Error initiating payment:', error);
        alert("Something went wrong with payment processing.");
        setProcessingPayment(false);
    }
};

// Simplify the clearCartAfterPayment function
const clearCartAfterPayment = async () => {
    const username = localStorage.getItem("username");
    if (!username) return;

    try {
        // Just refresh the cart - the order creation is now handled by the payment verification
        fetchCartItems();
    } catch (error) {
        console.error('Error refreshing cart:', error);
    } finally {
        setProcessingPayment(false);
    }
};

  if (loading) {
    return <div className="cart-container">Loading your cart...</div>;
  }

  return (
    <div className="cart-container">
      <h1 className="cart-title">Your Shopping Cart</h1>
      
      {cartItems.length === 0 ? (
        <div className="cart-empty">
          <p>Your cart is empty</p>
          <button onClick={() => navigate("/customer")} className="continue-shopping-btn">
            Continue Shopping
          </button>
        </div>
      ) : (
        <>
          <div className="cart-items">
            {cartItems.map((item) => (
              <div key={item.product.id} className="cart-item">
                <div className="item-image">
                  {item.product.photo ? (
                    <img src={item.product.photo} alt={item.product.name} />
                  ) : (
                    <div className="image-placeholder">No Image</div>
                  )}
                </div>
                
                <div className="item-details">
                  <h3 className="item-name">{item.product.name}</h3>
                  <p className="item-category">{item.product.category}</p>
                  <p className="item-price">Rs. {item.product.price} each</p>
                  <p className="item-description">{item.product.description}</p>
                </div>
                
                <div className="item-controls">
                  <div className="quantity-controls">
                    <button onClick={() => handleUpdateQuantity(item.product.id, item.quantity - 1)}>
                      -
                    </button>
                    <span className="quantity">{item.quantity}</span>
                    <button onClick={() => handleUpdateQuantity(item.product.id, item.quantity + 1)}>
                      +
                    </button>
                  </div>
                  
                  <p className="item-total">Rs.{item.itemTotal}</p>
                  
                  <button 
                    onClick={() => handleRemoveItem(item.product.id)}
                    className="remove-btn"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          <div className="cart-summary">
            <div className="summary-details">
              <h3>Order Summary</h3>
              <div className="summary-row">
                <span>Subtotal:</span>
                <span>Rs.{totalPrice}</span>
              </div>
              <div className="summary-row">
                <span>Shipping:</span>
                <span>Rs.{(totalPrice > 0 ? 50 : 0)}</span>
              </div>
              <div className="summary-row total">
                <span>Total:</span>
                <span>Rs.{(totalPrice > 0 ? totalPrice + 50 : 0)}</span>
              </div>
              
              <div className="payment-info">
                <p>🔒 Secure payment powered by Razorpay</p>
              </div>
              
              <button 
                onClick={initiateRazorpayPayment} 
                disabled={processingPayment || !razorpayLoaded}
                className="checkout-btn"
              >
                {processingPayment ? "Processing..." : "Proceed to Payment"}
              </button>
              
              <button onClick={() => navigate("/customer")} className="continue-shopping-btn">
                Continue Shopping
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default CartPage;