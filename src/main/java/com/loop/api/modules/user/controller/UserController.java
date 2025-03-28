package com.loop.api.modules.user.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.StandardResponse;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.service.UserService;
import com.loop.api.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// Get my profile
	@GetMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		UserResponse user = userService.getUserById(userPrincipal.getId());
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User profile fetched", user));
	}

	// Update my profile
	@PutMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<UserResponse>> updateMyProfile(
			@Valid @RequestBody UpdateUserProfileRequest profileRequest,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		UserResponse updatedUser = userService.updateUserProfile(userPrincipal.getId(), profileRequest);
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User profile updated", updatedUser));
	}

	// Delete my account
	@DeleteMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<Void>> deleteMyAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		userService.deleteUser(userPrincipal.getId());
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User deleted successfully", null));
	}
}
