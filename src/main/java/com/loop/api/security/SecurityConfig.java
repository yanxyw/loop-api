package com.loop.api.security;

import com.loop.api.modules.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	static final String[] EXCLUDED_PATHS = {
			"/public/**",
			"/auth/**",
			"/public/**",
			"/docs/**",
			"/actuator/**"
	};
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final UnauthorizedHandler unauthorizedHandler;
	private final ForbiddenHandler accessDeniedHandler;

	public SecurityConfig(JwtTokenProvider jwtTokenProvider,
						  UserRepository userRepository,
						  UnauthorizedHandler unauthorizedHandler,
						  ForbiddenHandler accessDeniedHandler) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
		this.unauthorizedHandler = unauthorizedHandler;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository,
				unauthorizedHandler);
		http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(unauthorizedHandler)
						.accessDeniedHandler(accessDeniedHandler)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(EXCLUDED_PATHS).permitAll()
						.anyRequest().authenticated()
				)
				// You can configure JWT filters or any other filters here
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:50469"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
