package com.smartcomplaint.controller;

import com.smartcomplaint.dto.AdminResponseRequest;
import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.StatusUpdateRequest;
import com.smartcomplaint.model.Category;
import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;

    public AdminComplaintController(ComplaintService complaintService, UserService userService) {
        this.complaintService = complaintService;
        this.userService = userService;
    }

    private User getAuthenticatedAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) {
            return null;
        }
        User user = userService.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.ADMIN) {
            return null;
        }
        return user;
    }

    /**
     * View all complaints with optional category and status filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Complaint>>> getAllComplaints(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ComplaintStatus status,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin privileges required"));
        }

        List<Complaint> list = complaintService.getFilteredComplaints(category, status);
        return ResponseEntity.ok(ApiResponse.ok("Complaints retrieved", list));
    }

    /**
     * View complaint details for admin.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Complaint>> getComplaint(
            @PathVariable Long id,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin privileges required"));
        }

        try {
            Complaint complaint = complaintService.getComplaintById(id);
            return ResponseEntity.ok(ApiResponse.ok("Complaint details retrieved", complaint));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update complaint status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Complaint>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest req,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin privileges required"));
        }

        try {
            Complaint updated = complaintService.updateStatus(id, req.getStatus(), req.getAssignedStaff());
            return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add or update admin response.
     */
    @PutMapping("/{id}/response")
    public ResponseEntity<ApiResponse<Complaint>> addResponse(
            @PathVariable Long id,
            @Valid @RequestBody AdminResponseRequest req,
            HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin privileges required"));
        }

        try {
            Complaint updated = complaintService.addAdminResponse(id, req.getResponse());
            return ResponseEntity.ok(ApiResponse.ok("Admin response saved successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get aggregate statistics for Admin overview.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats(HttpSession session) {
        User admin = getAuthenticatedAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin privileges required"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Admin statistics retrieved", complaintService.getAdminStats()));
    }
}
