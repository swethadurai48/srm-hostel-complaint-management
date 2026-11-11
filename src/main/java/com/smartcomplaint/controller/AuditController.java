package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.model.AuditLog;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AuditService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/erp/audit-logs")
public class AuditController {

    private final AuditService auditService;
    private final UserService userService;

    public AuditController(AuditService auditService, UserService userService) {
        this.auditService = auditService;
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
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentLogs(HttpSession session) {
        if (getAuthenticatedAdmin(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Audit logs retrieved", auditService.getRecentLogs()));
    }
}
