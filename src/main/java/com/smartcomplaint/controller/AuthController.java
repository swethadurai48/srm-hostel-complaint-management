package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.LoginRequest;
import com.smartcomplaint.dto.RegisterRequest;
import com.smartcomplaint.dto.UserResponse;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_KEY = "CURRENT_USER";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new student account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpSession session) {
        try {
            User user = userService.registerStudent(req);
            session.setAttribute(SESSION_USER_KEY, user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Registration successful", UserResponse.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Login for Student or Admin.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpSession session) {
        try {
            User user = userService.authenticate(req);
            session.setAttribute(SESSION_USER_KEY, user.getId());
            return ResponseEntity.ok(ApiResponse.ok("Login successful", UserResponse.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Logout and destroy active session.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    /**
     * Get currently logged-in user profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(ApiResponse.ok("Authenticated", UserResponse.fromEntity(user))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User session expired")));
    }

    /**
     * Update student profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody com.smartcomplaint.dto.ProfileUpdateRequest req,
            HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        try {
            User updated = userService.updateProfile(userId, req);
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", UserResponse.fromEntity(updated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
