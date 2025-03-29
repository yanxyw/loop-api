package com.loop.api.modules.user.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.StandardResponse;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - User Management", description = "Endpoints for admins to view, manage, and delete user accounts")
@RestController
@RequestMapping(ApiRoutes.Admin.USERS)
public class AdminUserController {

	private final UserService userService;

	public AdminUserController(UserService userService) {
		this.userService = userService;
	}

	// Get all users
	@Operation(
			summary = "Get all users (Admin only)",
			description = "Returns a list of all registered users. Requires ADMIN role."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Forbidden - user does not have ADMIN role"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<StandardResponse<List<UserResponse>>> getAllUsers() {
		List<UserResponse> users = userService.getAllUsers();
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "Fetched all users", users));
	}

	// Get a single user
	@Operation(
			summary = "Get user by ID (Admin only)",
			description = "Fetches the details of a specific user by their ID. Requires ADMIN role."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User fetched successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Forbidden - user does not have ADMIN role"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<StandardResponse<UserResponse>> getUserById(@PathVariable Long id) {
		UserResponse user = userService.getUserById(id);
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User fetched", user));
	}

	// Delete a user
	@Operation(
			summary = "Delete user by ID (Admin only)",
			description = "Deletes a specific user by their ID. Requires ADMIN role."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User deleted successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Forbidden - user does not have ADMIN role"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<StandardResponse<Void>> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok(StandardResponse.success(HttpStatus.OK, "User deleted successfully", null));
	}
}