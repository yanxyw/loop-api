package com.loop.api.modules.user.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.StandardResponse;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.service.UserService;
import com.loop.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "User", description = "Endpoints for authenticated users to view, update, and delete their account")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// Get my profile
	@Operation(
			summary = "Get my profile",
			description = "Returns the profile information of the currently authenticated user."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - user is not logged in"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@GetMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		UserResponse user = userService.getUserById(userPrincipal.getId());
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User profile fetched", user));
	}

	// Update my profile
	@Operation(
			summary = "Update my profile",
			description = "Updates the profile information of the currently authenticated user and returns the " +
					"updated profile."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Profile updated successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - user is not logged in"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@PutMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<UserResponse>> updateMyProfile(
			@Valid @RequestBody UpdateUserProfileRequest profileRequest,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		UserResponse updatedUser = userService.updateUserProfile(userPrincipal.getId(), profileRequest);
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User profile updated", updatedUser));
	}

	// Delete my account
	@Operation(
			summary = "Delete my account",
			description = "Deletes the currently authenticated user's account."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Account deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - user is not logged in"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@DeleteMapping(ApiRoutes.User.ME)
	public ResponseEntity<StandardResponse<Void>> deleteMyAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		userService.deleteUser(userPrincipal.getId());
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User deleted successfully", null));
	}
}
