package com.loop.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
	private final long expirationTime;
	private final SecretKey key;

	@Autowired
	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
							@Value("${jwt.expiration}") long expirationTime) {
		this.expirationTime = expirationTime;
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(UserPrincipal user) {
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("username", user.getUsername())
				.claim("isAdmin", user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(key)
				.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getUserIdFromToken(String token) {
		Jws<Claims> claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
		return claims.getPayload().getSubject();
	}
}