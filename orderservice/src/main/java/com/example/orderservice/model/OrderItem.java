package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

// OrderItem tablosunu temsil eden entity
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Her item benzersiz bir id’ye sahip

    private String productId; // ürün id
    private int quantity;     // ürün adedi

    // ManyToOne ile Order ile ilişkilendiriyoruz
    @ManyToOne
    @JoinColumn(name = "order_id") // foreign key kolonu
    @JsonBackReference
    private Order order;

    // Default constructor
    public OrderItem() {}

    // Kolay kullanım için constructor
    public OrderItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getter ve Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}

