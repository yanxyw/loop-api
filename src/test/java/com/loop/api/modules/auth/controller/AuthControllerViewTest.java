package com.loop.api.modules.auth.controller;

import com.loop.api.modules.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class AuthViewControllerTest {

	@Mock
	private AuthService authService;

	private AuthViewController controller;

	@BeforeEach
	void setup() {
		controller = new AuthViewController(authService);
	}

	@Test
	void shouldReturnSuccessViewWhenTokenIsValid() {
		// Given
		String token = "valid-token";
		Model model = new ExtendedModelMap();

		// When
		String viewName = controller.verifyEmail(token, model);

		// Then
		assertEquals("verify-result", viewName);
		assertEquals("success", model.getAttribute("status"));
		assertEquals("Your email address was successfully verified.", model.getAttribute("message"));

		verify(authService).verifyEmailToken(token);
	}

	@Test
	void shouldReturnErrorViewWhenTokenIsInvalid() {
		// Given
		String token = "invalid-token";
		Model model = new ExtendedModelMap();
		doThrow(new IllegalArgumentException("Invalid token")).when(authService).verifyEmailToken(token);

		// When
		String viewName = controller.verifyEmail(token, model);

		// Then
		assertEquals("verify-result", viewName);
		assertEquals("error", model.getAttribute("status"));
		assertEquals("This link is no longer valid. Please return to the app to request a new verification email.",
				model.getAttribute("message"));

		verify(authService).verifyEmailToken(token);
	}
}

