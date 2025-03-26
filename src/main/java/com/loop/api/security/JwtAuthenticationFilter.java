package com.loop.api.security;

import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		// Extract token from 'Authorization' header
		String token = resolveToken(request);

		// Validate token
		if (token != null && jwtTokenProvider.validateToken(token)) {
			Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
			User user = userRepository.findById(userId).orElse(null);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			UsernamePasswordAuthenticationToken authToken =
					new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}

		// Continue filter chain
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
