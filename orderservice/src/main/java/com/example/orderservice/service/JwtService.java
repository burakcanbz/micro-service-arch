package com.example.orderservice.service;

import io.jsonwebtoken.SignatureException;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;


@Service
public class JwtService {

    private final String secretKey = "supersecret_supersecretkey12345678"; // 32+ chars

    public Claims verifyToken(String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);

            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("Token is valid: " + claims);
            return claims;  // <-- RETURN Claims, not String
        } catch (Exception e) {
            throw new RuntimeException("Token verification failed", e);
        }
    }
}
