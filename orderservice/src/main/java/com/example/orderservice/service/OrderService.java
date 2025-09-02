package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Spring bean olarak işaretliyoruz, bu sayede DI ile kullanılabilir
public class OrderService {

    @Autowired // OrderRepository beanini Spring otomatik inject edecek
    private OrderRepository orderRepository;

    // 1. Belirli bir kullanıcıya ait tüm siparişleri getir
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    // 2. Tüm siparişleri sayfalama (pagination) ile getir
    public Page<Order> getAllOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size));
    }

    // 3. Belirli bir siparişi ID ile getir
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<OrderStatus> getOrderStatusByOrderId(Long orderId) {
        return orderRepository.findById(orderId).map(Order::getStatus);
    }

    // 4. Yeni bir sipariş oluştur
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED); // yeni sipariş statusünü CREATED yapıyoruz
        return orderRepository.save(order);   // save metodu ile persist ediyoruz
    }

    // 5. add Order Item to Order
    public Optional<Order> addOrderItemToOrder(Long orderId, OrderItem newItem){
        Optional <Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.addItem(newItem);

            orderRepository.save(order);
            return Optional.of(order);
        }
        return Optional.empty();
    }

    // 6. Mevcut bir siparişi iptal et
    public Optional<Order> cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.CANCELLED); // statusü CANCELLED yapıyoruz
            orderRepository.save(order);            // değişikliği kaydediyoruz
            return Optional.of(order);
        }
        return Optional.empty(); // sipariş bulunmazsa boş dön
    }

    // 7. Order'dan bir item silme
    public Optional<Order> removeItemFromOrder(Long orderId, Long itemId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // Silinecek item'ı bul
            OrderItem itemToRemove = order.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (itemToRemove != null) {
                // Order içinden çıkar ve ilişkiyi temizle
                order.removeItem(itemToRemove);

                // Değişiklikleri kaydet
                orderRepository.save(order);
            }

            return Optional.of(order);
        }

        return Optional.empty(); // order bulunamadı
    }
}
