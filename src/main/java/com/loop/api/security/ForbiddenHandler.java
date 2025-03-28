package com.loop.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.dto.response.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ForbiddenHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Autowired
	public ForbiddenHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request,
					   HttpServletResponse response,
					   AccessDeniedException accessDeniedException) throws IOException {

		StandardResponse<Void> errorResponse = StandardResponse.error(
				HttpStatus.FORBIDDEN,
				"Forbidden: You do not have permission."
		);

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType("application/json");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}