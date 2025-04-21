package com.loop.api.modules.auth.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

	private final AuthService authService;

	public AuthViewController(AuthService authService) {
		this.authService = authService;
	}

	@Operation(
			summary = "Verify user email",
			description = "Verifies a user's email using a token sent to their email address. "
					+ "If the token is valid and not expired, the user is marked as verified."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User successfully verified"),
			@ApiResponse(responseCode = "401", description = "Invalid or expired token"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@GetMapping(ApiRoutes.Auth.VERIFY)
	public String verifyEmail(@RequestParam String token, Model model) {
		try {
			authService.verifyEmailToken(token);
			model.addAttribute("status", "success");
			model.addAttribute("message", "Your email address was successfully verified.");
		} catch (Exception e) {
			model.addAttribute("status", "error");
			model.addAttribute("message", "This link is no longer valid. Please return to the app to request a new " +
					"verification email.");
		}
		return "verify-result";
	}
}