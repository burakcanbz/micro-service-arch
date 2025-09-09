package com.example.orderservice.model;

// Sipariş durumlarını tutan enum
public enum OrderStatus {
    CREATED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELED,
    FAILED,
    COMPLETED,
}
