package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.model.Notification;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.NotificationService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/erp/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        return userService.findById(userId).orElse(null);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", notificationService.getNotificationsForUser(user)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.ok("Unread count retrieved", Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        try {
            Notification notification = notificationService.markAsRead(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Marked as read", notification));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }
}
