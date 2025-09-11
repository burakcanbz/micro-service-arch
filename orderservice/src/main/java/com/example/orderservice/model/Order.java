package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Order tablosunu temsil eden ana entity
@Entity
@Table(name = "\"order\"") // çift tırnak ile PostgreSQL keyword kaçırılır
public class Order {

    // Primary key olarak kullanılacak id kolonunu tanımlıyoruz
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // kullanıcı alanı

    // Sipariş durumu için enum
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Order ile OrderItem arasındaki ilişkiyi tanımlıyoruz
    // mappedBy -> OrderItem içindeki 'order' alanı ile ilişkilendir
    // cascade -> Order silinirse veya kaydedilirse, item'lar da otomatik işlemlere dahil
    // orphanRemoval -> OrderItem Order'dan çıkarılırsa, db'den de silinsin
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>(); // boş liste ile başlatıyoruz, null gelmesin

    // Default constructor
    public Order() {}

    // Getter ve Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    // Kolay kullanım için item ekleme metodu
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this); // ilişkiyi iki yönlü güncelle
    }

    // Kolay kullanım için item çıkarma metodu
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null); // ilişkiyi kaldır
    }
}

