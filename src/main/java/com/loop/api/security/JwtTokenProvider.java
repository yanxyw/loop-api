package com.loop.api.security;

import com.loop.api.modules.user.model.User;
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
							@Value("${jwt.accessExpirationMs}") long expirationTime) {
		this.expirationTime = expirationTime;
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(UserPrincipal user) {
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("username", user.getUsername())
				.claim("profileUrl", user.getProfileUrl())
				.claim("isAdmin", user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(key)
				.compact();
	}

	public String generateToken(User user) {
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("username", user.getUsername())
				.claim("profileUrl", user.getProfileUrl())
				.claim("isAdmin", user.isAdmin())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(key)
				.compact();
	}

	public Jws<Claims> parseToken(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
	}

	public String getUserIdFromClaims(Jws<Claims> claims) {
		return claims.getPayload().getSubject();
	}
}