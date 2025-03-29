package com.loop.api.security;

import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final AuthenticationEntryPoint unauthorizedHandler;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
								   AuthenticationEntryPoint unauthorizedHandler) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
		this.unauthorizedHandler = unauthorizedHandler;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String token = resolveToken(request);

		if (token != null) {
			try {
				Jws<Claims> claims = jwtTokenProvider.parseToken(token);
				Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromClaims(claims));

				User user = userRepository.findById(userId)
						.orElseThrow(() -> new InsufficientAuthenticationException("User not found for token."));

				UserPrincipal userPrincipal = new UserPrincipal(user);
				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(userPrincipal, null,
								userPrincipal.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authToken);
			} catch (JwtException | IllegalArgumentException | InsufficientAuthenticationException e) {
				SecurityContextHolder.clearContext();
				unauthorizedHandler.commence(request, response,
						new InsufficientAuthenticationException("Invalid or expired access token.", e));
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
