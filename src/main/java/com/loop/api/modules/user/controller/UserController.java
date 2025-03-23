package com.loop.api.modules.user.controller;

import com.loop.api.common.dto.response.ApiResponse;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.service.UserService;
import com.loop.api.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get my profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse user = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile fetched", user));
    }

    // Update my profile
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestBody UpdateUserProfileRequest profileRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse updatedUser = userService.updateUserProfile(userPrincipal.getId(), profileRequest);
        return ResponseEntity.ok(ApiResponse.success("User profile updated", updatedUser));
    }

    // Delete my account
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
