package com.example.orderservice.model;

// Sipariş durumlarını tutan enum
public enum OrderStatus {
    CREATED,
    NEW,
    PROCESSING,
    COMPLETED,
    CANCELLED
}
