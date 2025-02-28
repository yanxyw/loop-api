package com.babelbeats.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final long expirationTime;
    private final Key key;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration}") long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .claims(Map.of("email", email))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }
}