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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

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

    private ObjectMapper mapper = new ObjectMapper();

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
        order.setStatus(OrderStatus.CREATED);
        logger.info("order creating !");
        Order createdOrder = orderRepository.save(order);

        try {
            String json = mapper.writeValueAsString(createdOrder);
            orderPublisher.publishOrderCreated(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Failed to publish order created event with order id: " + createdOrder.getId(), e);        }

        return createdOrder;
    }

    // 5. add Order Item to Order
    public Optional<Order> addOrderItemToOrder(Long orderId, OrderItem newItem){
        Optional <Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.addItem(newItem);
            Order itemAddedOrder = orderRepository.save(order);
            try{
                String json = mapper.writeValueAsString(itemAddedOrder);
                orderPublisher.publishOrderUpdated(json);
            }
            catch (Exception e) {
                logger.error("Failed to publish order updated event with order id: " + itemAddedOrder.getId(), e);
            }
            logger.info("Item added to order: " + orderId);
            return Optional.of(itemAddedOrder);
        }
        logger.warn("Order item not found : " + orderId);
        return Optional.empty();
    }

    // 6. Mevcut bir siparişi iptal et
    public Optional<Order> cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.CANCELED); // statusü CANCELLED yapıyoruz
            Order canceledOrder = orderRepository.save(order);            // değişikliği kaydediyoruz
            try{
                String json = mapper.writeValueAsString(canceledOrder);
                orderPublisher.publishOrderCanceled(json);
            }
            catch(Exception e) {
                logger.error("Failed to publish order canceled event with order id: " + canceledOrder.getId() , e);
            }

            logger.info("Order cancelled: " + orderId);
            return Optional.of(canceledOrder);
        }
        logger.warn("Order not found: " + orderId);
        return Optional.empty(); // sipariş bulunmazsa boş dön
    }

    // 7. Order'dan bir item silme
    public Optional<Order> removeItemFromOrder(Long orderId, Long itemId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            OrderItem itemToRemove = order.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (itemToRemove != null) {
                order.removeItem(itemToRemove);
                logger.info("Item removed from order: " + itemId);
                Order updatedOrder = orderRepository.save(order);
                try {
                    String json = mapper.writeValueAsString(updatedOrder);
                    orderPublisher.publishOrderUpdated(json);
                } catch (Exception e) {
                    logger.error("Failed to publish order updated event with order id: " + updatedOrder.getId(), e);
                }
                return Optional.of(updatedOrder);
            } else {
                logger.warn("Item not found in order: " + itemId);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public boolean removeOrderById(Long orderId){
        Optional<Order> optionalDeletingOrder = orderRepository.findById(orderId);
        try{
            String json = mapper.writeValueAsString(optionalDeletingOrder.get());
            orderPublisher.publishOrderDeleted(json);
        }
        catch (Exception e) {
            logger.error("Failed to publish order delete event with order id:" + optionalDeletingOrder.get().getId() , e);
        }
        if (optionalDeletingOrder.isPresent()) {
            orderRepository.delete(optionalDeletingOrder.get());
            return true;
        }
        return false;
    }
}
