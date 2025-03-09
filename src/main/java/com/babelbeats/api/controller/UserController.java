package com.babelbeats.api.controller;

import com.babelbeats.api.model.User;
import com.babelbeats.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users: Only admin
    @GetMapping
    @PreAuthorize("principal.isAdmin()")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get a single user: Admin or the user themselves
    @GetMapping("/{id}")
    @PreAuthorize("principal.isAdmin() or #id == principal.getId()")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Create a new user: Only admin
    @PostMapping
    @PreAuthorize("principal.isAdmin()")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // Update an existing user: Admin or the user themselves
    @PutMapping("/{id}")
    @PreAuthorize("principal.isAdmin() or #id == principal.getId()")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete a user: Admin or the user themselves
    @DeleteMapping("/{id}")
    @PreAuthorize("principal.isAdmin() or #id == principal.getId()")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
