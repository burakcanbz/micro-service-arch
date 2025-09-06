package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
/**
 * Ana uygulama sınıfı
 *
 * @SpringBootApplication notasyonu:
 * 1. @Configuration: Bu sınıfın Spring konfigürasyonu içerdiğini belirtir.
 * 2. @EnableAutoConfiguration: Spring Boot'un uygulaman için gerekli bean'leri otomatik oluşturmasını sağlar.
 * 3. @ComponentScan: Aynı paketteki veya alt paketlerdeki @Component, @Service, @Repository gibi bean'leri otomatik tarar.
 */
@SpringBootApplication
public class OrderserviceApplication {

    /**
     * Uygulamanın giriş noktası
     * @param args command line argümanları
     */
    public static void main(String[] args) {
        // Spring Boot uygulamasını başlatır
        SpringApplication.run(OrderserviceApplication.class, args);
    }

}
