package com.loop.api.modules.user.controller;

import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.security.UserPrincipal;
import com.loop.api.modules.user.service.UserService;
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
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse user = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }

    // Update my profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserProfileRequest profileRequest,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse updatedUser = userService.updateUserProfile(userPrincipal.getId(), profileRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete my account
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.ok("User deleted successfully");
    }
}
