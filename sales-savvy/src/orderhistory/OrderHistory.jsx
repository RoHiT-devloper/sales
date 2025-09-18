import React, { useState, useEffect } from "react";
import "./OrderHistory.css";

const OrderHistory = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchOrderHistory();
    }, []);

    const fetchOrderHistory = async () => {
        const username = localStorage.getItem("username");
        if (!username) {
            setError("Please log in to view order history");
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/orders/history?username=${username}`);
            if (response.ok) {
                const ordersData = await response.json();
                setOrders(ordersData);
            } else {
                setError("Failed to fetch order history");
            }
        } catch (error) {
            setError("Error fetching order history");
            console.error("Error:", error);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    if (loading) {
        return <div className="order-history-container">Loading your order history...</div>;
    }

    if (error) {
        return <div className="order-history-container error">{error}</div>;
    }

    return (
        <div className="order-history-container">
            <h1>Your Order History</h1>
            
            {orders.length === 0 ? (
                <div className="no-orders">
                    <p>You haven't placed any orders yet.</p>
                </div>
            ) : (
                <div className="orders-list">
                    {orders.map(order => (
                        <div key={order.id} className="order-card">
                            <div className="order-header">
                                <h2>Order #{order.id}</h2>
                                <span className="order-date">{formatDate(order.orderDate)}</span>
                            </div>
                            
                            <div className="order-details">
                                <div className="order-status">Status: {order.status}</div>
                                <div className="order-total">Total: Rs.{order.totalAmount}</div>
                            </div>
                            
                            <div className="order-items">
                                <h3>Items:</h3>
                                {order.items.map(item => (
                                    <div key={item.id} className="order-item">
                                        <div className="item-image">
                                            {item.product.photo ? (
                                                <img src={item.product.photo} alt={item.product.name} />
                                            ) : (
                                                <div className="image-placeholder">No Image</div>
                                            )}
                                        </div>
                                        <div className="item-details">
                                            <h4>{item.product.name}</h4>
                                            <p>Quantity: {item.quantity}</p>
                                            <p>Price: Rs.{item.priceAtTime} each</p>
                                        </div>
                                        <div className="item-total">
                                            Rs.{(item.priceAtTime * item.quantity).toFixed(2)}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default OrderHistory;