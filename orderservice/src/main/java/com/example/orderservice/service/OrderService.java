package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.logger.LoggerService;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.messaging.OrderPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Optional;

@Service // Spring bean olarak işaretliyoruz, bu sayede DI ile kullanılabilir
public class OrderService {

    @Autowired
    private OrderPublisher orderPublisher;

    @Autowired // OrderRepository beanini Spring otomatik inject edecek
    private OrderRepository orderRepository;

    @Autowired
    private LoggerService logger;

    // 1. Belirli bir kullanıcıya ait tüm siparişleri getir
    public List<Order> getOrdersByUserId(String userId) {
        logger.info("order getting by user id", userId);
        return orderRepository.findByUserId(userId);
    }

    // 2. Tüm siparişleri sayfalama (pagination) ile getir
    public Page<Order> getAllOrders(int page, int size) {
        logger.info("all orders getting !");
        return orderRepository.findAll(PageRequest.of(page, size));
    }

    // 3. Belirli bir siparişi ID ile getir
    public Optional<Order> getOrderById(Long orderId) {
        logger.info("order getting by id", orderId);
        return orderRepository.findById(orderId);
    }

    public Optional<OrderStatus> getOrderStatusByOrderId(Long orderId) {
        logger.info("order status getting by order id", orderId);
        return orderRepository.findById(orderId).map(Order::getStatus);
    }

    // 4. Yeni bir sipariş oluştur
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED); // yeni sipariş statusünü CREATED yapıyoruz
        logger.info("order creating !");
        Order createdOrder = orderRepository.save(order);   // save metodu ile persist ediyoruz
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", createdOrder.getId());
        orderPublisher.publishOrderCreated(orderData);
        return createdOrder;
    }

    // 5. add Order Item to Order
    public Optional<Order> addOrderItemToOrder(Long orderId, OrderItem newItem){
        Optional <Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.addItem(newItem);
            orderRepository.save(order);

            logger.info("Item added to order: " + orderId);
            return Optional.of(order);
        }
        logger.warn("Order item not found : " + orderId);
        return Optional.empty();
    }

    // 6. Mevcut bir siparişi iptal et
    public Optional<Order> cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.CANCELLED); // statusü CANCELLED yapıyoruz
            orderRepository.save(order);            // değişikliği kaydediyoruz

            logger.info("Order cancelled: " + orderId);
            return Optional.of(order);
        }
        logger.warn("Order not found: " + orderId);
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

                logger.info("item removed from order: " + itemId);
                // Değişiklikleri kaydet
                orderRepository.save(order);
            }
            logger.warn("item not found in order: " + itemId);
            return Optional.of(order);
        }

        return Optional.empty(); // order bulunamadı
    }
}
