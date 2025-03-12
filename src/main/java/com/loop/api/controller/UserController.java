package com.loop.api.controller;

import com.loop.api.dto.UpdateUserProfileRequest;
import com.loop.api.dto.UserResponse;
import com.loop.api.security.UserPrincipal;
import com.loop.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserResponse user = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }

    // Update my profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserProfileRequest profileRequest,
                                                Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserResponse updatedUser = userService.updateUserProfile(userPrincipal.getId(), profileRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete my account
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.ok("User deleted successfully");
    }
}
