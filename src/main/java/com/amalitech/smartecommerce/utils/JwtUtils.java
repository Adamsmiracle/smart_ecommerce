package com.amalitech.smartecommerce.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JwtUtils {
        private final SecretKey secretKey;
        private final long expirationTime;

        public JwtUtils(String secret, long expirationTimeInMs) {
            // Create a secure key from the secret string
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            this.expirationTime = expirationTimeInMs;
        }

        // Generate JWT token for a user
        public String generateToken(String username) {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationTime);

            Map<String, Object> claims = new HashMap<>();
            claims.put("username", username);
            claims.put("role", "USER"); // You can add custom claims

            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(secretKey)
                    .compact();
        }



        // Validate token and extract claims
        public Claims validateToken(String token) {
            try {
                return Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                throw new RuntimeException("Invalid or expired token: " + e.getMessage());
            }
        }


        // Extract username from token
        public String getUsernameFromToken(String token) {
            return validateToken(token).getSubject();
        }

        // Check if token is expired
        public boolean isTokenExpired(String token) {
            try {
                Date expiration = validateToken(token).getExpiration();
                return expiration.before(new Date());
            } catch (Exception e) {
                return true;
            }
        }
}
