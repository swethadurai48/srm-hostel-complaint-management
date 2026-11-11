package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/erp/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    private User getAuthenticatedAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        User user = userService.findById(userId).orElse(null);
        if (user == null || !user.getRole().isAdmin()) return null;
        return user;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(HttpSession session) {
        if (getAuthenticatedAdmin(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", userService.findAllUsers()));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null || !admin.getRole().isSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Super Admin privileges required to modify roles"));
        }
        try {
            Role newRole = Role.valueOf(body.get("role"));
            User updated = userService.updateUserRole(id, newRole);
            return ResponseEntity.ok(ApiResponse.ok("User role updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role or user ID"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createEnterpriseUser(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");
            String roleStr = body.getOrDefault("role", "EMPLOYEE");
            String department = body.get("department");

            Role role = Role.valueOf(roleStr);
            User created = userService.createEnterpriseUser(name, email, password, role, department);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
